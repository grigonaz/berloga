package cz.cvut.fel.berloga.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExecutionTimeInterceptor implements HandlerInterceptor {

    private final Logger LOGGER = LoggerFactory.getLogger(ExecutionTimeInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        request.setAttribute("startRequestTime", System.currentTimeMillis());

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        LOGGER.info( " execution time of " + request.getRequestURI() + ": "
                + ( System.currentTimeMillis() - (Long) request.getAttribute("startRequestTime"))
                + " ms" );

        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

}
