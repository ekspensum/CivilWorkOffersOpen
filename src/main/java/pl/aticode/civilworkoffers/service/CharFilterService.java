package pl.aticode.civilworkoffers.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CharFilterService {

    private final static Logger logger = LoggerFactory.getLogger(CharFilterService.class);
    private String charToReplace;

    @Autowired
    private MessageSource messageSource;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CharFilter {

        String[] forbiddenChar();

        ActionType action();
    }

    public enum ActionType {
        REMOVE, REPLACE;
    }

    /**
     * Method remove or replace forbidden characters entered by user in form
     * fields on page. Forbidden characters and action type are define in
     * annotation @CharFilter. This annotation can use only on field class for
     * type String. Method working with Java Reflection feature. Value of
     * annotated field has changed in selected object.
     */
    public void doCharFilter(StringBuilder forbiddenCharMessage, Object... objects) throws Exception {
        for (Object object : objects) {
            if (object instanceof HibernateProxy) {
                HibernateProxy hibernateProxy = (HibernateProxy) object;
                LazyInitializer initializer
                        = hibernateProxy.getHibernateLazyInitializer();
                object = initializer.getImplementation();
            }
            Class<?> clazz = object.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(CharFilter.class)) {
                    CharFilter annotation = field.getAnnotation(CharFilter.class);
                    String fieldValue = (String) field.get(object);
                    String[] forbiddenChar = annotation.forbiddenChar();
                    if (containForbiddenChar(fieldValue, forbiddenChar)) {
                        charToReplace = messageSource.getMessage("forbiddenCharMessage.charToReplace", null, LocaleContextHolder.getLocale());
                        ActionType action = annotation.action();
                        if (action.equals(ActionType.REMOVE)) {
                            String foundForbiddenCharToRemove = foundForbiddenChar(fieldValue, forbiddenChar, field);
                            for (int i = 0; i < forbiddenChar.length; i++) {
                                fieldValue = fieldValue.replace(String.valueOf(forbiddenChar[i]), "");
                            }
                            forbiddenCharMessage.append(messageSource.getMessage("forbiddenCharMessage.removed",
                                    new String[]{foundForbiddenCharToRemove, field.getName()}, LocaleContextHolder.getLocale()));
                        } else if (action.equals(ActionType.REPLACE)) {
                            String foundForbiddenCharToReplace = foundForbiddenChar(fieldValue, forbiddenChar, field);
                            for (int i = 0; i < forbiddenChar.length; i++) {
                                fieldValue = fieldValue.replace(forbiddenChar[i], charToReplace);
                            }
                            forbiddenCharMessage.append(messageSource.getMessage("forbiddenCharMessage.insert",
                                    new String[]{charToReplace, foundForbiddenCharToReplace, field.getName()}, LocaleContextHolder.getLocale()));;
                        } else {
                            throw new Exception("Illegal ActionType");
                        }
                        field.set(object, fieldValue);
                    }
                }
            }
        }
    }

//	PRIVATE METHODS	
    private boolean containForbiddenChar(String fieldValue, String[] forbiddenChar) {
        if (fieldValue == null) {
            return false;
        }
        for (int i = 0; i < fieldValue.length(); i++) {
            for (int j = 0; j < forbiddenChar.length; j++) {
                if (fieldValue.charAt(i) == forbiddenChar[j].charAt(0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String foundForbiddenChar(String fieldValue, String[] forbiddenChar, Field field) {
        StringBuilder forbiddenCharFound = new StringBuilder();
        for (int i = 0; i < fieldValue.length(); i++) {
            for (int j = 0; j < forbiddenChar.length; j++) {
                if (fieldValue.charAt(i) == forbiddenChar[j].charAt(0)) {
                    forbiddenCharFound.append(fieldValue.charAt(i)).append(" ");
                }
            }
        }
        logger.info("INFO from CharFilterService: {}", "Found forbidden char: " + forbiddenCharFound + " in field: " + field.getName());
        return forbiddenCharFound.toString();
    }
}
