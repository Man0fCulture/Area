from pydantic import BaseModel, Field

class UserCreate(BaseModel):
    email: str
    password: str
    firstName: str = Field(alias="firstName")
    lastName: str = Field(alias="lastName")

class UserLogin(BaseModel):
    email: str
    password: str

class UserResponse(BaseModel):
    id: str
    email: str
    firstName: str
    lastName: str

class AuthResponse(BaseModel):
    token: str
    user: UserResponse