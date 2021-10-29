package com.google.adapter.util;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public final class EnumLookup<E extends Enum<E>, D> {

  private final Class<E> enumClass;
  private final ImmutableMap<D, E> valueByIdMap;
  private final String idTypeName;

  private EnumLookup(Class<E> enumClass, ImmutableMap<D, E> valueByIdMap, String idTypeName) {
    this.enumClass = enumClass;
    this.valueByIdMap = valueByIdMap;
    this.idTypeName = idTypeName;
  }

  //region CONSTRUCTION
  public static <E extends Enum<E>, D> EnumLookup<E, D> of(
      Class<E> enumClass, Function<E, D> idExtractor, String idTypeName) {
    ImmutableMap<D, E> valueByIdMap = Arrays.stream(enumClass.getEnumConstants())
        .collect(ImmutableMap.toImmutableMap(idExtractor, Function.identity()));
    return new EnumLookup<>(enumClass, valueByIdMap, idTypeName);
  }

  public static <E extends Enum<E>> EnumLookup<E, String> byName(Class<E> enumClass) {
    return of(enumClass, Enum::name, "enum name");
  }

  public boolean contains(D id) {
    return valueByIdMap.containsKey(id);
  }

  public E get(D id) {
    E value = valueByIdMap.get(id);
    if (value == null) {
      throw new IllegalArgumentException(String.format(
          "No such %s with %s: %s", enumClass.getSimpleName(), idTypeName, id
      ));
    }
    return value;
  }

  public Optional<E> find(D id) {
    return Optional.ofNullable(valueByIdMap.get(id));
  }
  //endregion
}
