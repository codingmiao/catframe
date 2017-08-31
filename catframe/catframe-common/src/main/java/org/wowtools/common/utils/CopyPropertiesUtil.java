package org.wowtools.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 将两个class中相同类型、相同名称的属性进行copy的工具类，初始化时对反射作了缓存，因此性能比BeanUtils.
 * copyProperties方法高上百倍，建议高性能要求或大数据量时使用 。
 * 
 * 由于java中的泛型在编译后被擦除，为防止将List&lt;String&gt;拷贝到List&lt;Double&gt;之类的乌龙，
 * Collection的任何实现类均视为类型不同，不予拷贝。
 * 
 * @author liuyu
 *
 * @param <A>
 * @param <B>
 */
public class CopyPropertiesUtil<A, B> {
	private final Class<?> classA;
	private final Class<?> classB;
	private final Field[][] sameFields;
	private final Field[] adaptableFieldsA;
	private final Field[] adaptableFieldsB;

	public CopyPropertiesUtil(Class<? extends A> classA, Class<? extends B> classB) {
		this.classA = classA;
		this.classB = classB;
		List<Field> fieeldsA = getAdaptableFields(classA);
		List<Field> fieeldsB = getAdaptableFields(classB);
		List<Field[]> fs = new ArrayList<Field[]>(fieeldsA.size());
		for (Field fa : fieeldsA) {
			for (Field fb : fieeldsB) {
				if (fa.getName().equals(fb.getName()) && fa.getType().equals(fb.getType())
						&& !Collection.class.isAssignableFrom(fa.getType())) {
					fs.add(new Field[] { fa, fb });
					break;
				}
			}
		}
		Field[][] arr = new Field[fs.size()][2];
		fs.toArray(arr);
		sameFields = arr;

		Field[] f;

		f = new Field[fieeldsA.size()];
		fieeldsA.toArray(f);
		adaptableFieldsA = f;

		f = new Field[fieeldsB.size()];
		fieeldsB.toArray(f);
		adaptableFieldsB = f;

	}

	/**
	 * 获取一个class下所有的非公用、可修改字段，即非static、final字段
	 * 
	 * @param c
	 * @return
	 */
	private List<Field> getAdaptableFields(Class<?> c) {
		List<Field> list = new LinkedList<Field>();
		int skipFlag = 0x00000418;// final static abstract修饰的字段跳过
		for (; !c.equals(Object.class); c = c.getSuperclass()) {
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields) {
				if ((f.getModifiers() & skipFlag) == 0) {
					f.setAccessible(true);
					list.add(f);
				}
			}
		}
		return list;
	}

	/**
	 * 将b中的属性拷贝到a中的相应属性中
	 * 
	 * @param a
	 * @param b
	 */
	public void copyB2a(A a, B b) {
		try {
			for (Field[] sf : sameFields) {
				sf[0].set(a, sf[1].get(b));
			}
		} catch (Exception e) {
			throw new RuntimeException("copy properties exception", e);
		}
	}

	/**
	 * 将a中的属性拷贝到b中的相应属性中
	 * 
	 * @param a
	 * @param b
	 */
	public void copyA2b(A a, B b) {
		try {
			for (Field[] sf : sameFields) {
				sf[1].set(b, sf[0].get(a));
			}
		} catch (Exception e) {
			throw new RuntimeException("copy properties exception", e);
		}
	}

	public Class<?> getClassA() {
		return classA;
	}

	public Class<?> getClassB() {
		return classB;
	}

	public Field[] getAdaptableFieldsA() {
		return adaptableFieldsA;
	}

	public Field[] getAdaptableFieldsB() {
		return adaptableFieldsB;
	}

}
