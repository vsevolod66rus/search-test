package app.services

import cats.effect.kernel.Outcome.Succeeded
import cats.effect.{Deferred, IO}
import org.scalacheck.effect.PropF
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.mockito.cats.IdiomaticMockitoCats
import app.helpers.gen.Generators._
import app.helpers.TestLogger
import common.models.configs.ServiceConfig
import common.models.errors.ServerError.TooManyRequestsError
import org.http4s.client.Client

trait SearchAppSuite
    extends CatsEffectSuite
    with IdiomaticMockito
    with IdiomaticMockitoCats
    with ArgumentMatchersSugar
    with ScalaCheckEffectSuite {

  trait ExternalSearchServiceIO extends ExternalSearchService[IO]
  trait ErrorHandlerServiceIO   extends ErrorHandlerService[IO]
  trait ClientIO                extends Client[IO]
}

class SearchServiceSuite extends SearchAppSuite {

  class Env {
    val client: Client[IO] = mock[ClientIO]

    val serviceConfig: ServiceConfig                     = ServiceConfig(maxParallelInternalSearch = 1, maxParallelExternalSearch = 4)
    val externalSearchService: ExternalSearchService[IO] = mock[ExternalSearchServiceIO]

    val searchServiceF: IO[SearchService[IO]] = TestLogger
      .createRes[IO]
      .flatMap(implicit logger => SearchService[IO](externalSearchService, serviceConfig))
      .use(IO.pure)
  }

  test("search must reject the requests if too many request is computing") {
    PropF.forAllF(genTags, genSearchResponse) { (tags, searchResponse) =>
      val env = new Env
      for {
        // given
        searchService <- env.searchServiceF
        block1        <- Deferred[IO, Unit]
        block2        <- Deferred[IO, Unit]

        //when
        _                    = env.externalSearchService
                                 .handleSearch(tags)
                                 .returns(
                                   block1.complete(()) >> block2.get >> IO(searchResponse)
                                 )
        tooManyRequestsError =
          TooManyRequestsError(
            s"Too many requests error (${env.serviceConfig.maxParallelInternalSearch}) error occurred"
          )

        // then
        fib1                <- searchService.sofSearch(tags).attempt.start
        _                   <- block1.get
        fib2                <- searchService
                                 .sofSearch(tags)
                                 .onError(_ => block2.complete(()).void)
                                 .attempt
                                 .start

        res1 <- fib1.join.flatMap {
                  case Succeeded(result) => result
                  case _                 => fail("fib1 has been cancelled")
                }
        res2 <- fib2.join.flatMap {
                  case Succeeded(result) => result
                  case _                 => fail("fib2 has been cancelled")
                }
      } yield (res1, res2) match {
        case (Right(res), Left(err)) =>
          assertEquals(res, searchResponse)
          assertEquals(err, tooManyRequestsError)
        case _                       =>
          fail(s"res1 = $res1 res2 = $res2")
      }
    }
  }

  test("search must release semaphore if a computing error occurred") {
    PropF.forAllF(genTags, genSearchResponse) { (tags, searchResponse) =>
      val env = new Env
      for {
        // given
        searchService <- env.searchServiceF

        //when
        _     = env.externalSearchService
                  .handleSearch(tags)
                  .returns(IO.raiseError(new RuntimeException("Oops!")))
                  .andThen(IO(searchResponse))

        // then
        res1 <- searchService.sofSearch(tags).attempt
        res2 <- searchService.sofSearch(tags)
      } yield (res1, res2) match {
        case (Left(res1), res2) =>
          assert(res1.isInstanceOf[RuntimeException])
          assertEquals(res1.getMessage, "Oops!")
          assertEquals(res2, searchResponse)
        case _                  =>
          fail(s"res1 = $res1 res2 = $res2")
      }
    }
  }

}
