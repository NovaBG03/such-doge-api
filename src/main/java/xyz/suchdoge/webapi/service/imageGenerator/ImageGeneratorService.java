package xyz.suchdoge.webapi.service.imageGenerator;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import xyz.suchdoge.webapi.exception.DogeHttpException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ImageGeneratorService {
    private final ImageGeneratorProps config;
    private final RestTemplate restTemplate;

    public ImageGeneratorService(ImageGeneratorProps imageGeneratorConfig, RestTemplate restTemplate) {
        this.config = imageGeneratorConfig;
        this.restTemplate = restTemplate;
    }

    public byte[] generateProfilePic(String username) throws DogeHttpException {
        String url = "https://{domain}/{avatarType}/{size}/{username}?square&colors={colorPalette}";
        byte[] svgBytes = this.restTemplate.getForObject(url, byte[].class,
                this.config.getDomain(),
                this.config.getAvatarType(),
                this.config.getSizePx(),
                username,
                this.config.getColorPalette());

        if (svgBytes == null) {
            throw new DogeHttpException("CAN_NOT_GENERATE_PROFILE_PIC", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        byte[] pngBytes;
        try {
            pngBytes = convertSvgToPng(svgBytes, this.config.getSizePx(), this.config.getSizePx());
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_CONVERT_CORRECTLY_PROFILE_PIC", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return pngBytes;
    }

    private static byte[] convertSvgToPng(byte[] svgBytes, float height, float width)
            throws TranscoderException, IOException {
        ByteArrayOutputStream resultByteStream = new ByteArrayOutputStream();

        TranscoderInput transcoderInput = new TranscoderInput(new ByteArrayInputStream(svgBytes));
        TranscoderOutput transcoderOutput = new TranscoderOutput(resultByteStream);

        PNGTranscoder pngTranscoder = new PNGTranscoder();
        pngTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height);
        pngTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width);
        pngTranscoder.transcode(transcoderInput, transcoderOutput);

        resultByteStream.flush();
        return resultByteStream.toByteArray();
    }
}
