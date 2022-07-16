package tech.bananaz.bot.controllers;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultController implements Filter {
	
	@Value("${info.version:unknown}")
	private String appVersion;
	@Value("${info.name:unknown}")
	private String appName;
	private static final String SERVICE_HEADER = "X-SERVICE"; 
	private static final String SERVICE_VALUE_FORMAT = "%s/%s";
 
    @Override
    public void doFilter(ServletRequest request,
                        ServletResponse response,
                        FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
 
        res.addHeader(SERVICE_HEADER, String.format(SERVICE_VALUE_FORMAT, appName, appVersion));
        chain.doFilter(req, res);
    }
}
