Build Utils
======

Инструментарий для тестирования, работы с контейнерами и образами как в локальном окружении, так и в _CI_.

- `make_lib` - библиотеки к _Makefile_ для запуска _make targets_ с `docker run`, `docker-compose`, `docker build`, `docker push`.

```
make_lib/
├── utils_common.mk     Подключаяется в остальные `.mk` файлы. Содержит общие переменные и функции.
├── utils_container.mk  Инструментарий для запуска таргетов в контейнере с `docker run` или `docker-compose`.
└── utils_image.mk      Build и push контейнеров.
```

- `jenkins_lib` - библиотеки для включения в `Jenkinsfile`.

```
jenkins_lib/
├── pipeline.groovy        Основной файл для подключения, содержит pipeline() функцию.
├── runStage.groovy        Обертка над встроенной stage для корректного логирования и репортов в slack.
├── storeArtifacts.groovy  Сохранение артефактов Jenkins job (см. аргументы pipeline()).
└── storeCtLog.groovy      Красивое сохранине Erlang CT логов (пока не работает).
```

- `Jenkinsfile` - пример Jenkinsfile, ипользуеющего `build_utils`, и он же используется для тестирования `build_utils` репозитория в _Jenkins_.

- `examples` - пример использования `build_utils`.

```
examples/
├── Dockerfile.sh      Шаблон для генерации Dockerfile
├── Makefile           Пример Makefile, использующий make_lib.
├── docker-compose.sh  Шаблон для генерации docker-compose.yml
└── dummy_service.sh   Сервис заглушка как пример приложения
```

