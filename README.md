# Webhook Receiver Application

This application is a simple webhook receiver with two main endpoints. It collects JSON payloads from a `POST` request, stores them in memory, and sends them to an external endpoint based on configurable batch size and batch interval parameters.

## Endpoints

### 1. **GET /healthz**
- **Description**: This endpoint checks the health of the application.
- **Response**:
  - **Status**: 200 OK
  - **Body**: `"OK"`
  
### 2. **POST /log**
- **Description**: This endpoint receives JSON payloads and stores them in memory.
- **Behavior**:
  - The payloads are stored until either the **batch size** limit is reached or the **batch interval** time has elapsed.
  - Once one of these conditions is met, the collected payloads are forwarded to the configured **post endpoint**.
  - The stored data is then cleared after sending the batch.
  - If failed to forward payloads to configured endpoint, then application retries this upto 3 times with interval of 2 seconds. If failed 3 times then logs the error.

## Environment Variables

The behavior of the application is configurable using the following environment variables, which are defined in the `.env` file:

- **BATCH_SIZE**: The maximum number of payloads allowed in a batch.
- **BATCH_INTERVAL**: The maximum time (in milliseconds) to wait before sending a batch, even if the batch size is not reached.
- **POST_ENDPOINT**: The URL of the external endpoint to which the batches of payloads are forwarded.

### Example `.env` File:
```plaintext
BATCH_SIZE_ENV=2
BATCH_INTERVAL_ENV=10000
POST_ENDPOINT_ENV="http://localhost:5000/process-batch"
