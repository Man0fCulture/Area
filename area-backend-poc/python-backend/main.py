from fastapi import FastAPI, Depends, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
from typing import Optional
import time

from database import engine, Base, get_db
from models import User
from schemas import UserCreate, UserLogin, UserResponse, AuthResponse
from auth import create_access_token, get_password_hash, verify_password

Base.metadata.create_all(bind=engine)

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/api/auth/register", response_model=AuthResponse)
def register(user: UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(User).filter(User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    hashed_password = get_password_hash(user.password)
    db_user = User(
        email=user.email,
        password=hashed_password,
        first_name=user.firstName,
        last_name=user.lastName
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)

    access_token = create_access_token(data={"sub": user.email})

    return AuthResponse(
        token=access_token,
        user=UserResponse(
            id=str(db_user.id),
            email=db_user.email,
            firstName=db_user.first_name,
            lastName=db_user.last_name
        )
    )

@app.post("/api/auth/login", response_model=AuthResponse)
def login(user: UserLogin, db: Session = Depends(get_db)):
    db_user = db.query(User).filter(User.email == user.email).first()
    if not db_user or not verify_password(user.password, db_user.password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    access_token = create_access_token(data={"sub": user.email})

    return AuthResponse(
        token=access_token,
        user=UserResponse(
            id=str(db_user.id),
            email=db_user.email,
            firstName=db_user.first_name,
            lastName=db_user.last_name
        )
    )

@app.get("/about.json")
def about(request: Request):
    return {
        "client": {
            "host": request.client.host
        },
        "server": {
            "current_time": int(time.time()),
            "services": [
                {
                    "name": "fastapi",
                    "description": "Modern Python web framework",
                    "actions": [
                        {
                            "name": "user_registered",
                            "description": "A new user registers with FastAPI"
                        }
                    ],
                    "reactions": [
                        {
                            "name": "save_user",
                            "description": "Save user data with SQLAlchemy"
                        }
                    ]
                }
            ]
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8081)
