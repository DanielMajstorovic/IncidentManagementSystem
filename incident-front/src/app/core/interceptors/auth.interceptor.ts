import { Injectable } from "@angular/core"
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from "@angular/common/http"
import { Observable } from "rxjs"
import { AuthService } from "../services/auth.service"

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const token = this.authService.getToken()

    if (token && this.authService.isTokenValid(token)) {
      const authReq = req.clone({
        headers: req.headers.set("Authorization", `Bearer ${token}`),
      })
      return next.handle(authReq)
    }

    return next.handle(req)
  }
}
