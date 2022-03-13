package xyz.suchdoge.webapi.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import xyz.suchdoge.webapi.service.jwt.JwtProps;
import xyz.suchdoge.webapi.service.jwt.JwtService;

import java.util.List;

public class AutChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final JwtProps jwtConfig;

    public AutChannelInterceptor(JwtService jwtService, JwtProps jwtConfig) {
        this.jwtService = jwtService;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        List<String> tokenList = accessor.getNativeHeader(jwtConfig.getAuthTokenHeader());
        String authorizationToken = null;
        if(tokenList == null || tokenList.size() < 1) {
            return message;
        } else {
            authorizationToken = tokenList.get(0);
            if(authorizationToken == null) {
                return message;
            }
        }
        Authentication user = jwtService.getAuthentication(authorizationToken); // access authentication header(s)
        accessor.setUser(user);

        return message;
    }
}
