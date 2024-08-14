package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto;

public record UserDto(Integer id,
                      String username,
                      boolean enabled,
                      String roles) {
}
