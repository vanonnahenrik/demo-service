# demo-service
repo to reproduce an error when running with docker, build with `mvn spring-boot:build-image -Pnative -DskipTests` and run with `docker run --rm demo-service:0.0.1-SNAPSHOT`. It will fail with this error:

`org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory': Can't find bundle for base name org.eclipse.persistence.exceptions.i18n.EntityManagerSetupExceptionResource, locale en_US`
