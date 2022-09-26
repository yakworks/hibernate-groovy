<table><tr><td>

[![CircleCI](https://img.shields.io/circleci/project/github/yakworks/hibernate-groovy/master.svg?longCache=true&style=for-the-badge&logo=circleci)](https://circleci.com/gh/yakworks/hibernate-groovy) \
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.yakworks/hibernate-groovy/badge.svg?style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/org.yakworks/hibernate-groovy) \
[![9ci](https://img.shields.io/badge/BUILT%20BY-9ci%20Inc-blue.svg?longCache=true&style=for-the-badge)](http://9ci.com) \
[![9ci](https://img.shields.io/badge/GLUTEN-FREE-pink.svg?longCache=true&style=for-the-badge&logo=Atari)](http://9ci.com) \
[![Open Source](https://badges.frapsoft.com/os/v3/open-source.svg?v=103)](https://yak.works/)

</td>
<td>

<pre style="line-height: normal; background-color:#2b2929; color:#76ff00; font-family: monospace; white-space: pre; font-size: 10px">

              _.-````'-,_
          ,-'`           `'-.,_
  /)     (\       9ci's       '``-.
 ( ( .,-') )    YakWorks          ```,
  \ '   (_/                         !!
  |       /)           '           !!!
  ^\    ~'            '     !    !!!! 
    !      _/! , !   !  ! !  !   !!!   
    \Y,   |!!!  !  ! !!  !! !!!!!!!
      `!!! !!!! !!  )!!!!!!!!!!!!!
        !!  ! ! \( \(  !!!|/!  |/!
      /_(      /_(/_(    /_(  /_(   

            Version: 1.1
</pre>
</td></tr></table>

## hibernate-groovy-proxy

Gradle : `implementation "org.yakworks:hibernate-groovy-proxy:0.1"`

The problem is that the ByteBuddyInterceptor is not aware of groovy getMetaClass.
So a truthy check on the hibernate object or id check when called from dynamic groovy code will result in the interceptor initializing the code.

- overrides the `ByteBuddyInterceptor` to check for the getMetaClass call. 
- intercepts the `toString` so it doesn't hydrate the proxy. so when its a proxy will always show something like `Customer: 1 (proxy)`
  where the class is the simple name, followed by id and a proxy indicator in parens. 

Normally the following would fail the `!Hibernate.isInialized` calls but with this plugin it will work

```groovy
when:
Customer proxy = session.load(Customer, 1L)

then:
//without ByteBuddyGroovyInterceptor this would normally cause the proxy to init
proxy
proxy.metaClass
!Hibernate.isInitialized(proxy)
//without ByteBuddyGroovyInterceptor any call will hydrate the proxy
// in dynamically compiled groovy, when in a @CompileStatic .getId and .id work just like java.
// with this lib they all work
proxy.id == 1
proxy.getId() == 1
proxy["id"] == 1
!Hibernate.isInitialized(proxy)
//this would also normally cause the proxy to hydrate
proxy.toString() == "Customer : 1 (proxy)"
!Hibernate.isInitialized(proxy)
```

### hibernate-groovy-proxy config

can set properties in the `hibernate.properties` or if using Spring or Grails in the application.(yml|properties).

- `hibernate.groovy.proxy.enabled` : (default: true) Completely disables the ByteBuddyGroovyInterceptor.
- `hibernate.groovy.proxy.replace_to_string`: (default: true) Always replace the toString when its a proxy

NOTE: When using Spring JPA the hibernate settings are under the prefix `spring.jpa.properties`.
so for example in the application.properties set `spring.jpa.properties.hibernate.groovy.proxy.replace_to_string=false`
to turn off the interceptors toString replacement. 

## hibernate-groovy-db

Gradle : `implementation "org.yakworks:hibernate-groovy-db:<<version>>"`

misc helpers for hibernate, all require groovy. 
includes the dependency for hibernate-groovy-proxy

### yakworks.hibernate.h2.ExtendedH2Dialect

registers columns types (used for json types)

- Types.OTHER -> "json"
- Types.VARBINARY -> "BLOB"

### yakworks.hibernate.schema.SimpleSchemaManagementTool

Filters out the foreign key generation when db is created. Used to make it easier in testing H2
set `hibernate.schema_management_tool: 'yakworks.hibernate.schema.SimpleSchemaManagementTool'`

### JsonType

make its easier to set the JsonType in Gorm mapping blocks. 
you will need to add the appropriate dependency to your project along with the . 
see https://github.com/vladmihalcea/hibernate-types


## Developer Notes

### Build and Tests

> While gradle is the underlying build tool, `make` and [ship-kit](https://github.com/yakworks/shipkit) is used for consitency across projects and languages for versioning, docs and deployment. 

- We use `make` to wrap `gradle`.
- after cloning run `make check`, `./gradlew check` will also work.   
- `make` with no target to see help. 

> **NOTE:** Make might need to be upgraded to a version later than 3.8. 
> The console message should advise you on using brew. You can always fall back to gradle and use the Makefile as docs on build utils.
