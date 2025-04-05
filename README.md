# План запуска и работы с client revenue analyzer

## Обязательно к прочтению для каждого участника проекта
> **Важно**: Если над задачей работа не ведется более двух недель, то она снимается с разработчика и возвращается обратно в ready for development.
### Чтобы успешно пройти ревью созданного вами pull request убедитесь, что каждый пункт выполнен
- [ ] Создать отдельную ветку, в которой название будет идентично названию задачи и ее номеру
- [ ] Собрать проект локально через Maven, убедиться в том, что все тесты прошли успешно.
- [ ] Просмотреть свой pull request в GitHub для выявления "некачественного" кода (лишние импорты, дублирование, опечатки, неиспользуемый код, неверно выбранная директория).
- [ ] Добавить reviewer, assignees и задачу в development
- [ ] Убедиться в том, что все status check в GitHub прошли и осталось только получить approve.
- [ ] Убедиться в том, что на новый функционал добавлен тест и он не перетирает проверки других тестов (лучше всего написать отдельный тест).
- [ ] Если добавляется новый endpoint, проверить, что для него добавлена документация в Swagger, добавлен запрос в Postman.

## Настройка интеграции с Google Calendar

> **Важно:** Храните файл с ключами доступа локально и не отправляйте коммиты с этими файлами в ветку. Для этого можно использовать папку `data` в корне проекта, которая уже добавлена в `.gitignore` и не будет отслеживаться GIT.

### Обязательные шаги в google cloud console
1. Создать service account в Google Cloud Console.
2. Включить Calendar API.
3. Сформировать OAuth2 файл в формате JSON с ключами доступа к этому аккаунту.
4. Сохранить файл локально. Можно использовать папку data
5. В настройке `app.google.calendar.serviceAccountFilePath` указать путь к этому файлу.
6. Добавить в google calendar сервисный аккаунт в список тех, кому доступен выбранный календарь, выдав полные права доступа. Делается это в настройках календаря. E-mail сервисного аккаунта взять из Cloud Console.

### Обязательные (минимальные) настройки для базовой работы приложения
1. В настройке `app.google.calendar.mainCalendarId` указать ID календаря, к которому требуется получить доступ.
2. Включить интеграцию календаря `app.google.calendar.integration.enabled`
3. Задать цвета для запланированных мероприятий `app.calendar.color.planned`. <br>
Цвета указаны в файле `calendar-integration/src/main/resources/GoogleEventColors.txt` <br>
Базовые цвета:
   1. Стандартный цвет - запланированное мероприятие
   2. Желтый (banana) - требующее перенос
   3. Красный (tomato) - отмененное  

## Дополнительный функционал
### Автоматический бэкап мероприятий
1. Выставить `app.calendar.events.backup.enabled=true`

### Телеграм бот
1. Выставить `bot.enabled=true`
2. Указать значение для `bot.secret-token`. Значение должно соответствовать значению настройки `app.secret-token`
3. Зарегистрировать своего бота в @BotFather
4. Указать в `bot.api-token` значение токена, которое было получено в результате выполнения пункта 3.

### Настройка интеграции с ИИ от GigaChat
1. Зарегистрируйтесь в личном кабинете Sber Studio, используя свой аккаунт Сбер ID.
2. Создайте проект в левом меню.
3. Выберите GigaChat API в разделе AI-модели.
4. В открывшемся окне:
   - Введите название проекта.
   - Ознакомьтесь и примите условия пользовательского соглашения.
5. Сгенерировать ключ авторизации (Authorization key).
6. Указать в `giga-chat.auth-key` ключ авторизации.

## Список тумблеров для включения функционала
| Название настройки                      | Описание функционала                                   |
|-----------------------------------------|--------------------------------------------------------|
| app.secret-token.enabled                | Включает проверку секретного токена в каждом запросе   |
| app.google.calendar.integration.enabled | Включает интеграцию с google calendar                  |
| app.calendar.events.backup.enabled      | Включает проведение автоматических бэкапов мероприятий |
| bot.enabled                             | Включает telegram бота                                 |
| giga-chat.auth-key                      | Ключ для авторизации при взаимодействии с GigaChat API |


## Вспомогательные функции
### Swagger

В приложении активен Swagger и доступен по пути: [http://localhost:8080/revenue-analyzer/swagger-ui/index.html](http://localhost:8080/revenue-analyzer/swagger-ui/index.html)

### ELK-стек
Для выведения логов с помощью elk-стека проставьте elk.enabled=true \
Предварительно создайте контейнеры в Docker с помощью docker/docker-compose.yml
(для загрузки образов может потребоваться vpn)