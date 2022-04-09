package xyz.suchdoge.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import xyz.suchdoge.webapi.service.blockchain.BlockIoProps;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainProps;
import xyz.suchdoge.webapi.service.imageGenerator.ImageGeneratorProps;
import xyz.suchdoge.webapi.service.jwt.JwtProps;
import xyz.suchdoge.webapi.service.register.RegisterProps;
import xyz.suchdoge.webapi.service.storage.AwsStorageProps;

@SpringBootApplication()
@EnableConfigurationProperties({
        JwtProps.class,
        AwsStorageProps.class,
        RegisterProps.class,
        ImageGeneratorProps.class,
        BlockIoProps.class,
        DogeBlockchainProps.class
})
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
