# Getting Started

Projects created by start.spring.io contain Spring Boot, a framework that makes Spring ready to work inside your app, but without much code or configuration required. Spring Boot is the quickest and most popular way to start Spring projects. If you have any questions, please contact zdu.strong@gmail.com.

## Development environment setup
1. From https://adoptium.net install java v21, and choose Entire feature.<br/>
2. From https://code.visualstudio.com install Visual Studio Code.<br/>
   Next, install extension "XML" and "Extension Pack for Java".<br/>
3. From https://www.docker.com install docker. Next, run the command:<br/>

    docker run -d -p 9010:9010 gcr.io/cloud-spanner-emulator/emulator

4. Optional.<br/>
   From https://dbeaver.io install DBeaver.<br/>

## Available Scripts

In the project directory, you can run:

### `./mvn clean spring-boot:run`

Let’s build and run the program. Open a command line (or terminal) and navigate to the folder where you have the project files. We can build and run the application by issuing the command.

The last couple of lines here tell us that Spring has started. Spring Boot’s embedded Apache Tomcat server is acting as a webserver and is listening for requests on 127.0.0.1 port 8080. Open your browser and in the address bar at the top enter http://127.0.0.1:8080. You should get a nice friendly response like this:
"Hello, World!"

### `./mvn clean package`

To generate executable jar package

### `./mvn clean test`

Run unit tests

### `java -jar diff.jar`

Generate database version upgrade sql.<br/>

Its source code is in the ".mvn/database_diff" folder.<br/>

### `java -jar diff.jar --onlyResetDatabase`

Delete all tables in the development database<br/>

### `./mvn versions:update-properties versions:update-parent`

Check that a new version of the dependency is available<br/>

The following dependencies are currently unable to continue to be upgraded:<br/>

## Notes - How to use vscode

1. Press "F5" to start.<br/>
2. In debug mode, "F10" Step over, "F11" Step into, "Shipt+F11" Step out, "F5" Skip breakpoint.<br/>
3. Rebuild the project:<br/>

    Views - Commands Palette - Java: Clean Java Language Server Workspace

## Notes - jinq - Things to note

Some experience in use, if you already know it, you can skip it.
1. Before the new JPA entity, query all the required data and save the previous entity.
2. Never use getList of jpa entity, always query data from the database, avoid memory overflow and data expiration.
3. Jinq does not support nested select statements(select * from (select * from user)). This doesn't matter because it doesn't need to be used.
4. Jinq does not support union and union all. It doesn't matter, we can add database association table for query.
5. Jinq doest not support right join. This does not matter, we can use left join.
6. Jinq doest not support outer join. It doesn't matter, we can add database association table for query.

## Notes - jpa - create entity

    this.persist(userEntity);

## Notes - jpa - update entity

    this.merge(userEntity);

## Notes - jpa - delete entity

    this.remove(userEntity);

## Notes - jinq - getOnlyValue

get only one element, like this:

    this.streamAll(UserEmailEntity.class).getOnlyValue();

## Notes - jinq - findFirst

get the first element, like this:

    this.streamAll(UserEntity.class).findFirst();

## Notes - jinq - toList

get array

    this.streamAll(UserEntity.class).toList();

get model array

    this.streamAll(UserEntity.class).map(s -> this.userFormatter.format(s)).toList();

## Notes - jinq - pagination

    JPAJinqStream<UserEntity> stream = this.streamAll(UserEntity.class);
    return new PaginationModel<>(1, 10, stream, (s) -> s.getUsername());

## Notes - jinq - exists

    this.streamAll(UserEntity.class).exists();

## Notes - jinq - where

    this.streamAll(UserEntity.class).where(s -> s.getUsername().equals("tom"));

## Notes - jinq - and

    this.streamAll(UserEntity.class).where(s -> s.getUsername().contains("jerry") && s.getUsername().contains("tom"));

    this.streamAll(UserEntity.class).where(s -> s.getUsername().contains("jerry")).where(s -> s.getUsername().contains("tom"));

## Notes - jinq - or

    this.streamAll(UserEntity.class).where(s -> s.getUsername().contains("tom") || s.getUsername().contains("jerry"));

## Notes - jinq - or of array

    public long getUsers(List<String> names) {
        JPAJinqStream<UserEntity> stream = this.streamAll(UserEntity.class);
        JPAJinqStream<UserEntity> streamOne = stream;
        for (String name : names) {
            JPAJinqStream<UserEntity> streamTwo = streamOne.where(s -> s.getUsername().contains(name));
            stream = stream == streamOne ? streamTwo : stream.orUnion(streamTwo);
        }
        return stream.count();
    }

## Notes - jinq - inner join

    this.streamAll(UserEntity.class).join((s, t) -> t.stream(UserEmailEntity.class));

    this.streamAll(UserEmailEntity.class).join(s -> JinqStream.of(s.getUser()));

    this.streamAll(UserEntity.class).joinList(t -> t.getUserEmailList());

    this.streamAll(UserEmailEntity.class).where(s -> s.getUser().getUsername().equals("tom"));

## Notes - jinq - left join

    this.streamAll(UserEntity.class).leftOuterJoin((s, t) -> t.stream(UserEmailEntity.class),
        (s, t) -> s.getId().equals(t.getId()));

    this.streamAll(UserEmailEntity.class).leftOuterJoin(s -> JinqStream.of(s.getUser()));

    this.streamAll(UserEntity.class).leftOuterJoinList((s) -> s.getUserEmailList());

## Notes - jinq - group by

Group by id, username

    var stream = this.streamAll(UserEntity.class).group(s -> new Pair<>(s.getId(), s.getUsername()), (s, t) -> t.count());
    return new PaginationModel<>(1, 15, stream, (s) -> {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", s.getOne().getOne());
        map.put("username", s.getOne().getTwo());
        map.put("countOfThisGroup", s.getTwo());
        return map;
    });

## Notes - jinq - order by

Sort by username first, then by id

    this.streamAll(UserEntity.class).sortedBy(s -> s.getId()).sortedBy(s -> s.getUsername());

## Notes - jinq - order by with complex statistical conditions

    this.streamAll(OrganizeEntity.class).select((s, t) ->
        new Pair<>(
            s,
            t.stream(OrganizeEntity.class)
                .where(m -> m.getOrganizeShadow().getName().contains(s.getOrganizeShadow().getName()))
                .count()
        )
    )
    .sortedBy(s -> s.getOne().getId())
    .sortedBy(s -> s.getTwo())
    .toList();

## Notes - jinq - Use subqueries in where

    this.streamAll(UserEntity.class).where((s, t) ->
        t.stream(UserEmailEntity.class).where(m -> m.getUser().getId().equals(s.getId())).exists()
    );

    this.streamAll(UserEntity.class).where( s ->
        JinqStream.from(s.getUserEmailList()).where(m -> m.getEmail().equals("tom@gmail.com")).exists()
    );

## Notes - jinq - Format entity to model

All data can be obtained and set to the model, support any structure

    public UserModel format(UserEntity userEntity) {
        var userId = userEntity.getId();
        var email = this.streamAll(UserEmailEntity.class)
                .where(s -> s.getUser().getId().equals(userId))
                .where(s -> s.getIsActive())
                .sortedDescendingBy(s -> s.getId())
                .sortedDescendingBy(s -> s.getUpdateDate())
                .select(s -> s.getEmail())
                .findFirst()
                .orElse(StringUtils.EMPTY);
        var userModel = new UserModel().setId(userEntity.getId()).setUsername(userEntity.getUsername()).setEmail(email);
        return userModel;
    }

## Notes - Params date

javascript:

    axios.get("/abc", {
        params: {
            date: new Date()
        }
    })

java:

    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.format.annotation.DateTimeFormat.ISO;
    @GetMapping("/abc")
    public ResponseEntity<?> abc(@RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) Date date) {
        return ResponseEntity.ok().build();
    }

    URI url = new URIBuilder("/abc").setParameter("date", FastDateFormat.getInstance(dateFormatProperties.getUTC()).format(new Date())).build();
    ResponseEntity<Object> response = this.testRestTemplate.getForEntity(url, Object.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());

## Notes - id - Generate unique ordered uuid of version 1

    import com.fasterxml.uuid.Generators;

    Generators.timeBasedReorderedGenerator().generate().toString()

## Notes - Accept timezone from javascript, then convert to utc offset. UTC offset can be passed as a parameter to database methods.

javascript:

    const { timeZone } = Intl.DateTimeFormat().resolvedOptions()

    Asia/Shanghai

java:

    var utcOffset = this.timeZoneUtil.getUtcOffset("Asia/Shanghai");

    +08:00

# Notes - Throws an exception with the specified status code

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The specified task does not exist");

## Notes - multi-process programming

    var command = "npm --version";
    if (SystemUtils.IS_OS_WINDOWS) {
        command = "cmd /c " + command;
    }
    var env = EnvironmentUtils.getProcEnvironment();
    env.put("CustomerEnv", "GREEN");
    DefaultExecutor.builder().get().execute(CommandLine.parse(command), env);

## Learn More

1. Jinq (http://www.jinq.org/docs/queries.html)
2. Learn SQL (https://www.sqlcourse.com)
3. Deeplearning4j (https://deeplearning4j.konduit.ai)
