package cn.lw.fz.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(value = ElementType.METHOD)
public @interface AutoMethodLog {
}
