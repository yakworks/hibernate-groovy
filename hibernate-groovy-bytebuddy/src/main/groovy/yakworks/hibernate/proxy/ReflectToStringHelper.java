/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package yakworks.hibernate.proxy;

import javax.persistence.Transient;
import java.beans.Introspector;
import java.lang.reflect.*;
import java.util.Locale;
import java.util.regex.Pattern;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.property.access.internal.PropertyAccessStrategyMixedImpl;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.Type;

/**
 * Used org.hibernate.internal.util.ReflectHelper as basis, which only support equals and hashcode
 * this helps to check if toString is overriden so that by default we can support toString
 * without it hydrating the proxy.
 */
@SuppressWarnings("unchecked")
public final class ReflectToStringHelper {

	public static final Class[] NO_PARAM_SIGNATURE = new Class[0];
	public static final Object[] NO_PARAMS = new Object[0];

	private static final Method OBJECT_TOSTRING;

	static {
		Method toString;
		try {
            toString = extractToStringMethod( Object.class );
		}
		catch ( Exception e ) {
			throw new AssertionFailure( "Could not find Object.toString()", e );
		}
        OBJECT_TOSTRING = toString;
	}

	/**
	 * Disallow instantiation of ReflectHelper.
	 */
	private ReflectToStringHelper() {
	}

	/**
	 * Encapsulation of getting hold of a class's {@link Object#toString toString} method.
	 *
	 * @param clazz The class from which to extract the toString method.
	 * @return The toString method reference
	 * @throws NoSuchMethodException Should indicate an attempt to extract toString method from interface.
	 */
	public static Method extractToStringMethod(Class clazz) throws NoSuchMethodException {
		return clazz.getMethod( "toString", NO_PARAM_SIGNATURE );
	}

	/**
	 * Determine if the given class defines a {@link Object#toString} override.
	 *
	 * @param clazz The class to check
	 * @return True if clazz defines an toString override.
	 */
	public static boolean overridesToString(Class clazz) {
		Method hashCode;
		try {
			hashCode = extractToStringMethod( clazz );
		}
		catch ( NoSuchMethodException nsme ) {
			return false; //its an interface so we can't really tell anything...
		}
		return !OBJECT_TOSTRING.equals( hashCode );
	}

}
