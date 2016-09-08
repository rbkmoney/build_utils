Build Utils [![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/build_utils/master)](http://ci.rbkmoney.com/job/rbkmoney_private/job/build_utils/job/master/)
======

Инструментарий для тестирования, работы с контейнерами и образами как в локальном окружении, так и в _CI_.

## Содержание build_utils библиотеки

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
├── as_user.sh     Create specific user in container and run command as this user.
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

## Как подключать в свой проект

1. Добавить git сабмодуль:
    ```
git submodule add -b master git@github.com:rbkmoney/build_utils.git build_utils
```

1. Обновить Makefile, выставляя необходимые переменные и подключая библиотеки. Например, для `erlang` проекта:
    ```make
SUBMODULES = build_utils
SUBTARGETS = $(patsubst %,%/.git,$(SUBMODULES))

UTILS_PATH := build_utils
TEMPLATES_PATH := .

# Name of the service
SERVICE_NAME := {{my_service}}
# Service image default tag
SERVICE_IMAGE_TAG ?= $(shell git rev-parse HEAD)
# The tag for service image to be pushed with
SERVICE_IMAGE_PUSH_TAG ?= $(SERVICE_IMAGE_TAG)

# Base image for the service
BASE_IMAGE_NAME := service_erlang
BASE_IMAGE_TAG := $(BASE_IMAGE_TAG) # Replace with the current version (tag) for service_erlang image!

BUILD_IMAGE_TAG := $(BUILD_IMAGE_TAG) # Replace with the current version (tag) for build image!

CALL_ANYWHERE := all submodules rebar-update compile xref lint dialyze test start devrel release clean distclean

# Hint: 'test' might be a candidate for CALL_W_CONTAINER-only target
CALL_W_CONTAINER := $(CALL_ANYWHERE)

.PHONY: $(CALL_W_CONTAINER)

all: compile

-include $(UTILS_PATH)/make_lib/utils_container.mk
-include $(UTILS_PATH)/make_lib/utils_image.mk

$(SUBTARGETS): %/.git: %
	git submodule update --init $<
	touch $@

submodules: $(SUBTARGETS)
```

    Еще примеры _Makefile'ов_ для `erlang` из [erlang-service-template](https://github.com/rbkmoney/erlang-service-template/blob/master/erlang-service-files/Makefile) и [hellgate](https://github.com/rbkmoney/hellgate/blob/master/Makefile).


1. Добавить `Dockerfile.sh` шаблон в корень своего репозитория, взяв за основу [examples/Dockerfile.sh](./examples/Dockerfile.sh). Поправить инструкции:
    * __MAINTAINER__
    * __COPY__
    * __CMD__
    * __WORKDIR__

    > Не забыть:
    > ```chmod 755 Dockerfile.sh```

1. Добавить `docker-compose.sh` шаблон в корень своего репозитория, взяв за основу [examples/docker-compose.sh](./examples/docker-compose.sh). Если есть необходимость, добавить в описание сервисы, от которых зависит ваш.

    > Не забыть:
    > ```chmod 755 docker-compose.sh```

1. Добавить `Jenkinsfile` в корень своего репозитория. Смотри пример [Jenkinsfile](./Jenkinsfile). Готовый стандандартынй [Jenkinsfile](https://github.com/rbkmoney/erlang-service-template/blob/master/erlang-service-files/Jenkinsfile) `erlang` проекта (надо заменить `{{name}}` на _название проекта_).

1. Для удобства добавить в `.gitignore` правила из [.gitignore](./.gitignore).


### Hint:
Далее при необходимости обновить версию сабмодулей (взять последнюю доступную на бранче):
```
git submodule update --remote
```

