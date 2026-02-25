package com.example.auth.filter;

import com.example.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;



import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String authHeader  = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

        // 3. Извлечь токен (убрать "Bearer ")
        String token =  authHeader.substring(7);

    if (jwtService.validateToken(token)){
        String username = jwtService.extractUsername(token);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username,null, new ArrayList<>());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);







    }
        filterChain.doFilter(request, response);




    }

}
// работает так что  наследуюсь от абстрактного класса  в котором обязательная реализация метода
// doFilterInternal и проверяю из параметра request хедер если его нет и он не соответсует
// условиям то я говорю иди дальше по фильтру, если соотвествует то я беру и извлекаю токен
// из хедера, если токен валдиный (моя функция валидации ) то достаю имя пользователя
// ( тоже моя функция ) и на основе этого создаю обьект класса UsernamePasswordAuthenticationToken
// 3 переданный( даже пустой параметр говорит что пользователь прошел авторизацию )
//  добавил доп параметры в виде айпишника пользоваетля,  и потом кладу этот обьект в
// SecurityContextHolder чтобы спринг смотрел на этот атрибут, и если он его видит значит
// пользователь авторизован  в будущем спринг смотрит на него если он есть
// то пользователь может стучаться в эндпоинты которые доступны только авторизованным


