# Users API

### Usage

Start the API with:
```sh
sbt run
```

Run the tests with:
```sh
sbt test
```

### Current implementation

The current implementation consists of an AdminApi designed for an administrator and a PublicApi designed for end users. The service methods exposed differ between those two apis, depending on the intended user.  

The AdminApi exposes the following endpoints:
```
GET     /admin/users
GET     /admin/users/:id
DELETE  /admin/users/:id
POST    /admin/users/:id/reset-password
POST    /admin/users/:id/block
POST    /admin/users/:id/unblock
```

The PublicApi exposes the following endpoints:
```
POST    /public/users/sign-up
GET     /public/users/me
PUT     /public/users/me/email
PUT     /public/users/me/password
```

With the exception of the sign-up endpoint, all endpoints exposed by the PublicApi expect to be passed a user id in place of a bearer token (`Authorization: Bearer ${user_id}`). Failure to do so will result in a 401 response.

### Things I'd have liked to implement or improve given more time

##### End user authentication
I would have used session-based authentication and added a sign-in endpoint to the PublicApi. This endpoint should accept credentials via basic auth, check whether they correspond to an existing user and if so, create a new session and return its id in a JWT token. Subsequent requests to the PublicApi should include this token in an `Authorization` header.  

The sessions could be persisted in a registry similar to the one for Users. I would use something like this:
```scala
// defined in src/main/scala/users/domain/Session.scala
final case class Session(
  id: Session.Id,
  userId: User.Id,
  expires: OffsetDateTime
)
object Session {
  final case class Id(value: String) extends AnyVal
}

// defined in src/main/scala/users/persistence/repositories/package.scala
type SessionRepository = sessions.Repository
val SessionRepository = sessions.Repository

// defined in src/main/scala/users/persistence/repositories/sessions/InMemoryRepository.scala
private[sessions] object InMemoryRepository {
  private final val SessionMap: TrieMap[Session.Id, Session] =
    TrieMap.empty
}
```

##### Admin user authentication
This could be achieved by adding basic auth to the AdminApi endpoints and defining admin credentials in the configuration as follows:
```
admin {
  username = ${ADMIN_USERNAME}
  password = ${ADMIN_PASSWORD}
}
```

##### Return types
In their current implementation, the apis simply return json-encoded versions of the domain types, such as `User`. This means that attributes such as a user's `password` or `metadata` are exposed to the outside world. The former should probably never be exposed, and the latter only to administrators. I would have added return types (eg: `UserOutput`) to the api domain which would omit certain attributes that we don't want users to access.

##### Code duplication
There is a fair amount of code duplication in the apis. I've had to define new methods for the sole purpose of mapping  `users.services.usermanagement.Error` instances to `users.api.domain.HttpError` ones. I would have preferred instead to wrap service calls in a function like the one below but could not get this to compile:
```scala
def serviceCallWrapper[F[_]: Functor, A](f: => F[Error Either A]): F[HttpError Either A] =
  EitherT[F, Error, A](f)
    .leftMap[HttpError](_.asHttpError)
    .value
// error: value asHttpError is not a member of Error
```
