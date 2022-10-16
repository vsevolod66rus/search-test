# sof-search-app

Используемые технологии: cats-effect, http4s, fs2, circe, sttp, scalacheck.

Веб-сервис поиска по заданным ключевым словам через внешний веб-сервис - с помощью обращения к [REST API StackOverflow](https://api.stackexchange.com/docs/search).

В параметрах запроса передается параметр `tag`, содержащий ключевой тэг для поиска. 
Параметров может быть несколько, в этом случае запросы к внешнему веб-сервису будут выполняться параллельно, 
но при этом не открывая более указанного в конфиге числа соединений.

В результате работы веб-сервиса возвращается суммарная статистика по всем тэгам - 
сколько раз встречался тег во всех вопросах и сколько раз на вопрос, содержащий тэг, был дан ответ.

При запуске веб-сервиса генерируется спецификация API через Swagger: [http://host:port/docs/](http://127.0.0.1:8080/docs/)

## API
### GET /search

#### пример запроса: 

`http://127.0.0.1:8080/search?tag=scala&tag=java&tag=python`

#### примеры ответов:
200 OK:
```json
{
  "scala": {
    "total": 100,
    "answered": 50
  },
  "java": {
    "total": 100,
    "answered": 7
  },
  "python": {
    "total": 100,
    "answered": 6
  }
}
```

200 OK:
```json
{
  "scala": {
    "total": 100,
    "answered": 50
  },
  "java": {
    "total": 100,
    "answered": 7
  },
  "python": "An error occurred while perform the search"
}
```

429 Too Many Requests:
```json
{
  "status": 429,
  "statusText": "TooManyRequests",
  "message": "На данный момент выполняется максимальное количество запросов. Пожалуйста, попробуйте выполнить Ваш запрос через несколько секунд."
}
```

## Использование
`git clone https://github.com/vsevolod66rus/search-test.git`

`cd search-test/`

`sbt`

`project sofSearchApp;compile;run`

хост, порт, таймаут клиента, ограничение на кол-во одновременных 
запросов к /search, максимальное кол-во пареллельных запросов к внешнему сервису, 
теги для запроса к внешнему сервису задаются в `application.conf`