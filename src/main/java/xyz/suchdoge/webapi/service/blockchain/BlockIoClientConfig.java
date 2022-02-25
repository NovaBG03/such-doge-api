package xyz.suchdoge.webapi.service.blockchain;

import lib.blockIo.BlockIo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockIoClientConfig {
    private final BlockIoConfig config;

    public BlockIoClientConfig(BlockIoConfig blockIoConfig) {
        this.config = blockIoConfig;
    }

    @Bean
    public BlockIo dogeBlockIo() throws Exception {
        return new BlockIo(this.config.getDogeApiKey(), this.config.getPin(), this.config.getApiVersion());
    }

//    @Bean
//    public BlockIo ltcBlockIo() throws Exception {
//        return null;
//    }
}
