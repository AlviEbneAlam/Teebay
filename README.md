# Teebay

An online buying, selling, and renting commodities store. Users can list their products for sale or rent on an hourly/daily basis. Features include editing/deleting products, browsing available products, and buying or renting them. Users can also view their activity such as sold, bought, borrowed, and lent products.

---

## Prerequisites

- React v18.13.1 or above  
- JDK 17  
- PostgreSQL 17.4  
- Maven v3.6.3  

> **Note:** You can update Java or DB versions via `pom.xml` if needed.

---

## Backend Setup

1. Clone the repository.

2. Navigate to `Teebay/teebay-backend`.

3. Open `src/main/resources/application.properties`.

4. Update PostgreSQL database name, URL, username, and password.

5. (Optional) Specify a schema name; otherwise, the default `public` schema will be used.

6. Run the backend server:

   - Using an IDE, run the Spring Boot application, **or**
   - From terminal, navigate to `teebay-backend` folder and run:

     ```bash
     mvn spring-boot:run
     ```

---

## Frontend Setup

1. Navigate to `Teebay/teebay-frontend`.

2. (Optional) Open `vite.config.ts` to change the default port if needed.

3. Open terminal inside the frontend folder.

4. Install dependencies:

   ```bash
   npm install
   npm run dev
   
5. Go to localhost:{port}/ to go to the index page of the application

