# Expense Tracker

## Run the React frontend

Start the Spring Boot backend on `http://localhost:8080`, then run:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite. Its development proxy forwards `/api` requests
to the backend, so no separate CORS setup is needed during local development.

The page uses user ID `1` by default and supports listing, creating, updating,
and deleting entries, as well as importing HDFC bank statement PDFs. To point
the frontend at a different backend URL, set `VITE_API_BASE_URL` (for example,
`http://localhost:8080/api/expense`) in a `frontend/.env` file.
