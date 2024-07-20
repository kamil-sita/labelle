package place.sita.labelle.datasource.impl.jooq.binding;

import org.apache.commons.lang3.NotImplementedException;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.List;

public class TypeUtil {

	private TypeUtil() {

	}

	public static <TypeT> TypeT toCommonType(Field<TypeT> field, Object value) {
		return convert(value, field.getType());
	}

	public static <TypeT> List<TypeT> toCommonType(Field<TypeT> field, List<Object> value) {
		List<TypeT> collectionInType = new ArrayList<>();
		for (Object object : value) {
			collectionInType.add(convert(object, field.getType()));
		}
		return collectionInType;
	}

	public static <TypeT, ResultT> ResultT withCommonType(Field<TypeT> field, Object value, FunctionOnCommonType<ResultT, TypeT> function) {
		TypeT valueInType = convert(value, field.getType());
		return function.apply(field, valueInType);
	}

	public static <TypeT, ResultT> ResultT withCommonType(Field<TypeT> field, List<Object> value, FunctionOnCommonTypeCollection<ResultT, TypeT> function) {
		List<TypeT> collectionInType = new ArrayList<>();
		for (Object object : value) {
			collectionInType.add(convert(object, field.getType()));
		}
		return function.apply(field, collectionInType);
	}

	private static <TypeT> TypeT convert(Object value, Class<TypeT> type) {
		if (type == String.class) {
			return (TypeT) value.toString();
		} else {
			throw new NotImplementedException();
		}
	}

	@FunctionalInterface
	public interface FunctionOnCommonType<ResultT, TypeT> {
		ResultT apply(Field<TypeT> field, TypeT value);
	}

	@FunctionalInterface
	public interface FunctionOnCommonTypeCollection<ResultT, TypeT> {
		ResultT apply(Field<TypeT> field, List<TypeT> value);
	}

}
