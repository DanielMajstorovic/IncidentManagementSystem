export interface User {
  id: string
  name: string
  email: string
  role: "USER" | "MODERATOR" | "ADMIN"
  avatar?: string
}

export interface JwtPayload {
  iss: string
  sub: string
  email: string
  role: "USER" | "MODERATOR" | "ADMIN"
  name: string
  iat: number
  exp: number
}