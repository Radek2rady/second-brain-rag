# API Documentation

Base URL for all endpoints is `/api/documents`.
All requests and responses use `application/json` unless stated otherwise.

## 1. Document Management

### Upload File (Background Processing)
Upload a `.txt`, `.md`, or `.pdf` file. The server processes and chunks the file asynchronously in the background.

- **URL:** `/upload`
- **Method:** `POST`
- **Headers:** `Content-Type: multipart/form-data`
- **Payload:**
  - `file`: The file to upload.
- **Success Response:**
  - **Code:** 202 Accepted
  - **Content:**
    ```json
    {
      "status": "processing",
      "jobId": "uuid-string",
      "message": "Soubor 'example.pdf' se zpracovává na pozadí..."
    }
    ```

### Get Job Status
Check the status of a specific background upload/ingestion job.

- **URL:** `/upload/status/{jobId}`
- **Method:** `GET`
- **Success Response:**
  - **Code:** 200 OK
  - **Content:**
    ```json
    {
      "jobId": "uuid-string",
      "fileName": "example.pdf",
      "status": "PROCESSING", 
      "totalChunks": 150,
      "processedChunks": 45,
      "progressPercent": 30.0,
      "errorMessage": ""
    }
    ```
  *Note: Possible statuses are `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`.*

### Get All Active Jobs
Get the status of all current and past upload jobs.

- **URL:** `/upload/status`
- **Method:** `GET`
- **Success Response:**
  - **Code:** 200 OK
  - **Content:** Array of job status objects (same schema as above).

### Get All Documents
Returns a list of all indexed vector chunks in the database.

- **URL:** `/`
- **Method:** `GET`
- **Success Response:**
  - **Code:** 200 OK
  - **Content:**
    ```json
    [
      {
        "id": "uuid-string",
        "content": "Vector chunk text content...",
        "metadata": {
          "raw": "{\"source\": \"document.txt\"}"
        }
      }
    ]
    ```

### Delete Document Chunk
Deletes a specific chunk by its UUID.

- **URL:** `/{id}`
- **Method:** `DELETE`
- **Success Response:**
  - **Code:** 200 OK
  - **Content:**
    ```json
    {
      "status": "success",
      "message": "Document uuid-string deleted"
    }
    ```

---

## 2. Search & Chat

### Hybrid Semantic Search
Performs a hybrid search (Full-Text + Vector Similarity) and returns raw document chunks matching the query. Employs LLM-based query expansion before searching.

- **URL:** `/search`
- **Method:** `GET`
- **URL Params:**
  - `query=[string]` (Required)
  - `topK=[int]` (Optional, default 4)
- **Success Response:**
  - **Code:** 200 OK
  - **Content:** Array of `VectorDocument` objects.

### RAG Chat
Initiates or continues a conversation. Asks a question against the indexed documents using ChatGPT, providing exact references.

- **URL:** `/chat`
- **Method:** `GET`
- **URL Params:**
  - `query=[string]` (Required)
  - `conversationId=[string]` (Optional. If omitted, a new ID is generated and returned.)
- **Success Response:**
  - **Code:** 200 OK
  - **Content:**
    ```json
    {
      "answer": "Generated LLM response...",
      "conversationId": "uuid-string",
      "source": "LOCAL",
      "references": ["document.txt", "contract.pdf"]
    }
    ```

---

## 3. Chat History

### Get Conversations
Returns a list of all unique conversation IDs stored in the database.

- **URL:** `/conversations`
- **Method:** `GET`
- **Success Response:**
  - **Code:** 200 OK
  - **Content:** `["uuid-1", "uuid-2"]`

### Get Chat History
Returns the message history for a specific conversation ID.

- **URL:** `/chat/history`
- **Method:** `GET`
- **URL Params:**
  - `conversationId=[string]` (Required)
- **Success Response:**
  - **Code:** 200 OK
  - **Content:**
    ```json
    [
      {
        "role": "user",
        "content": "What is the warranty period?"
      },
      {
        "role": "assistant",
        "content": "Based on ..."
      }
    ]
    ```

---

## 4. Legacy/Manual Endpoints
*These endpoints are typically bypassed by `/upload` but remain available for raw text injection.*

### Save Single Abstract Document
- **URL:** `/`
- **Method:** `POST`
- **Payload:** `{"content": "Text", "metadata": {"key": "value"}}`

### Bulk Ingest Raw Text
Chunks raw text directly from the request body.
- **URL:** `/ingest`
- **Method:** `POST`
- **Payload:** `{"text": "Very long text...", "metadata": {}}`
