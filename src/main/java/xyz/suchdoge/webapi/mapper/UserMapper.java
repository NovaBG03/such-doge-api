package xyz.suchdoge.webapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.suchdoge.webapi.dto.user.RegisterUserDto;
import xyz.suchdoge.webapi.model.DogeUser;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    DogeUser registerUserDtoToDogeUser(RegisterUserDto registerUserDto);
}
