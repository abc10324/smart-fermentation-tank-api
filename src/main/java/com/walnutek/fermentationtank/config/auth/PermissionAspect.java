package com.walnutek.fermentationtank.config.auth;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.entity.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Aspect
public class PermissionAspect {

    @Before("@within(hasRole)")
    public void onClassCheck(JoinPoint joinPoint, HasRole hasRole) {
        roleCheck(joinPoint, hasRole);
    }

    @Before("@annotation(hasRole)")
    public void onMethodCheck(JoinPoint joinPoint, HasRole hasRole) {
        roleCheck(joinPoint, hasRole);
    }

    private void roleCheck(JoinPoint joinPoint, HasRole hasRole) {
        boolean isInvalid = true;

        if(Objects.nonNull(Auth.getAuthUser()) && Objects.nonNull(hasRole)) {
            var role = Auth.getAuthUser().getRole();
            List<User.Role> acceptedRoleList = List.of();

            if(!List.of(hasRole.roles()).isEmpty()) {
                acceptedRoleList = List.of(hasRole.roles());
            } else if(!List.of(hasRole.value()).isEmpty()) {
                acceptedRoleList = List.of(hasRole.value());
            }

            if(acceptedRoleList.isEmpty()) {
                isInvalid = false;
            } else {
                isInvalid = !acceptedRoleList.contains(role);
            }
        } else {
            isInvalid = true;
        }

        if(isInvalid) {
            throw new AppException(AppException.Code.E003);
        }
    }
}
