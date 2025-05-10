## 🧪 HTTP Server – CodeCrafters Challenge (Java)

[![progress-banner](https://backend.codecrafters.io/progress/http-server/199516b4-2916-4f27-bb50-02e77f4bcd96)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

This repository contains my completed solution to the [CodeCrafters HTTP Server challenge](https://app.codecrafters.io/courses/http-server/overview) implemented in **Java**. The challenge involves building a fully-functional HTTP/1.1 server from scratch, handling features such as request parsing, routing, persistent connections, and compression.

### 🚀 Features Implemented

- ✅ HTTP/1.1 compliant request parser
- ✅ Persistent connection (`keep-alive`)
- ✅ Header parsing (Content-Length, User-Agent, etc.)
- ✅ Routing: `/echo/:value`, `/user-agent`, `/files/:filename`
- ✅ Gzip compression via `Accept-Encoding: gzip`
- ✅ File read/write operations via `GET`/`POST` on `/files`
- ✅ Graceful handling of `Connection: close`

---

### 📦 Usage

#### ⚙️ Build & Run

```bash
javac -d out $(find . -name "*.java")
java -cp out dev.santhoshle.http.HttpServer --directory=./tmp
```

Then test with:

```bash
curl --http1.1 -v http://localhost:4221/echo/banana
curl --http1.1 -v http://localhost:4221/user-agent -H "User-Agent: blueberry/apple-blueberry"
```

---

### 📁 Directory Structure

```
.
├── dev/
│   └── santhoshle/
|       └──http/
|          ├── HttpConstants.java
│          ├── HttpServer.java
│          ├── Request.java
│          ├── Response.java
│          └── Utils.java
├── tmp/       # Used for /files file operations
└── README.md
```

---

### 🧠 Learnings

- Hands-on low-level HTTP protocol experience
- Java I/O, socket programming, and concurrency
- Request parsing and stream handling
- Code design with SOLID principles

---

### 📜 License

This project is part of the [CodeCrafters.io](https://codecrafters.io) challenge and is shared for educational purposes.
