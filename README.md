Build Utils
======

Инструментарий для тестирования, работы с контейнерами и образами как в локальном окружении, так и в _CI_.


- `jenkins_lib` - библиотеки для включения в `Jenkinsfile`.

```
jenkins_lib/
├── pipeDefault.groovy     Простейший pipeline для работы с использованием docker registry.
└── storeArtifacts.groovy  Сохранение gz сжатых артефактов Jenkins job.
```


- `Jenkinsfile` - пример Jenkinsfile, использующего `build_utils`, и он же используется для тестирования `build_utils` репозитория в _Jenkins_.


- `make_lib` - библиотеки к _Makefile_ для запуска _make targets_ с `docker run`, `docker-compose`, `docker build`, `docker push`.

```
make_lib/
├── utils_common.mk     Подключается в остальные `.mk` файлы. Содержит общие переменные и функции.
├── utils_container.mk  Инструментарий для запуска таргетов в контейнере с `docker run` или `docker-compose`.
├── utils_image.mk      Работа с образами (build, push).
└── utils_repo.mk       Клонирование или синхронизация отдельных репозиториев.
```


- `sh/` - useful shell scripts (e.g. used by make_lib)

```
sh/
├── functions.sh   Common functions library.
├── getstage3.sh   Download any latest stage3 and optionaly do something with it.
└── repo-init.sh   Clone or sync any git repository.
```


- `examples` - пример использования `build_utils`.

```
examples/
├── Dockerfile.sh      Шаблон для генерации Dockerfile.
├── Makefile           Пример Makefile, использующий make_lib.
├── docker-compose.sh  Шаблон для генерации docker-compose.yml.
└── dummy_service.sh   Сервис-заглушка, как пример приложения.
```

