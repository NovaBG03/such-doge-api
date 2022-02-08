package xyz.suchdoge.webapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class HashingServiceTest {

    HashingService hashingService = new HashingService();

    @ParameterizedTest
    @DisplayName("Should return the same hash for the same words")
    @ValueSource(strings = {"test", " test2 ", "2323i9daj-D#", "!@###$$%%*(&(*))_)*&", "@%$$Q$C#%^&N*M&^M(&*)M#$ DFDFS DFSADFSDFDTD $%", ""})
    void shouldReturnTheSameHashForTheSameWords(String string) {
        String hashed1 = hashingService.hashString(string);
        String hashed2 = hashingService.hashString(string);

        assertEquals(hashed1, hashed2);
    }

    @ParameterizedTest
    @DisplayName("Should return different hash for different words")
    @ValueSource(strings = {"test", " test2 ", "2323i9daj-D#", "!@###$$%%*(&(*))_)*&", "@%$$Q$C#%^&N*M&^M(&*)M#$ DFDFS DFSADFSDFDTD $%", ""})
    void shouldReturnDifferentHashForDifferentWords(String string) {
        final String newString = string + " ";
        String hashed1 = hashingService.hashString(newString);
        String hashed2 = hashingService.hashString(string);

        assertNotEquals(hashed1, hashed2);
    }
}
