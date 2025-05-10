## Project Structure & Design Decisions

The original structure of the Spring Boot project has been preserved, but I made several improvements.

### Key Architectural Decisions

- **Separated Interfaces for Controller and Service Layers**  
  I introduced interfaces (`ItemController`, `ItemService`) to improve code navigation and readability.  

- **Organized Controller Layer**
  - `controller/ItemController` → defines the endpoints
  - `controller/impl/ItemControllerImpl` → contains the actual implementation logic

- **Service Layer Structured Similarly**
  - `service/ItemService` → interface for business logic
  - `service/impl/ItemServiceImpl` → implementation, includes async processing

- **Validation**
  - Custom annotation `@UniqueEmail` backed by `UniqueEmailValidator`
  - Ensures email uniqueness before persisting an item

- **Exception Handling**
  - Centralized via `ApiExceptionHandler`
  - Custom exceptions: `EmailAlreadyExistsException`, `ObjectNotFoundException`

- **Swagger (Planned)**
  - The structure is designed with future integration of Swagger/OpenAPI in mind.
  - Interfaces will allow easy exposure of documentation without modifying implementation classes.

---

## Code Coverage Summary

| Package                   | Line Coverage | Notes                                     |
|---------------------------|---------------|-------------------------------------------|
| `controller.impl`         | 66%           | Item endpoints tested (e.g., GET, POST)   |
| `service.impl`            | 54%           | Core business logic partially covered     |
| `repository`              | 54%           | Coverage through integration tests        |
| `exception`               | 48%           | Some global error handling paths untested |
| `config`, `model`, `dto`  | 100%          | DTOs, models, and configs fully used      |
| `validator`               | 75%           | Custom validator partially tested         |


### Unique Email Validation

To ensure that each item has a unique email address, a custom annotation was implemented. This annotation checks whether the provided email already exists in the database.
If you attempt to create multiple items with the same email address, a validation exception will be thrown to prevent duplicates.
When updating an existing item, validation logic is applied to ensure email uniqueness:
  - If the email remains unchanged (same as the current item), the update proceeds normally.
  - If a new email is provided and it already exists in the database, a validation exception is thrown to prevent duplicates.

### ModelMapper Configuration

A `ModelMapper` configuration is included in the `config` package to facilitate object mapping when working with the database. 
It allows easy conversion between `ItemDTO` and `Item` entities, ensuring that data is correctly mapped when saving to or retrieving from the database.

### ItemDTO Usage

Within the `controller` package, there is a subpackage containing `ItemDTO` class. The DTO is used for communication between the controller and service layers. 
It helps ensure that only the necessary fields are transferred, making the application more efficient and secure. 
This approach is also useful for future integration with a frontend application, where only required fields should be exposed.

### Exception Handling

A dedicated `exception` package is created to manage application errors in a structured way.
`ApiException` is a custom object that contains only the essential information for each error: `message`, `HTTP status`, and `timestamp`.
`ApiExceptionHandler` extends `ResponseEntityExceptionHandler` and is annotated with `@ControllerAdvice`. This enables global exception handling across the entire application.
Each specific exception scenario (e.g., `handleBadRequestException`) is handled by a method annotated with `@ExceptionHandler`. These methods return a structured `ApiException` object, improving error clarity and making it easier to understand the error source.

### Validation on Item Entity

The `Item` class is annotated with various validators to ensure data integrity at the time of input. These validations include:
  - Ensuring that fields are not empty.
  - Verifying that the email field is not already used and has a specific format.

### Asynchronous Processing with CompletableFuture

In the `processItemsAsync` method, `CompletableFuture.supplyAsync` is used instead of `runAsync` because it provides more flexibility. While `supplyAsync` can be used like `runAsync`, the reverse is not possible, as `supplyAsync` returns a result, which makes it more versatile.
My informations sources were: 
  - https://concurrencydeepdives.com/completablefuture-runasync-supplyasync/
  - https://medium.com/javarevisited/java-completablefuture-c47ca8c885af
  - https://www.baeldung.com/java-completablefuture-runasync-supplyasync

One of the assignment requirements was to use a thread-safe collection. I chose `HashMap` to store the items that have not yet been processed. Items that have on status "PROCESSED" were ignored, because I need to print just the items with an updated status.
Additionally, the return type of the method was updated to `CompletableFuture<?>`, since methods using `CompletableFuture` should return it. The final result is extracted and handled in the controller layer.




