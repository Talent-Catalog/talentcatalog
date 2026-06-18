package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.anonymization.model.CandidateRegistration;
import org.tctalent.anonymization.model.EducationType;
import org.tctalent.server.model.db.mapper.CandidateMapper;
import org.tctalent.server.model.db.mapper.CandidateMapperImpl;
import org.tctalent.server.model.db.mapper.CountryMapperImpl;
import org.tctalent.server.model.db.mapper.EducationLevelMapperImpl;
import org.tctalent.server.model.db.mapper.EducationMajorMapperImpl;
import org.tctalent.server.model.db.mapper.EnumsMapperImpl;
import org.tctalent.server.model.db.mapper.LanguageLevelMapperImpl;
import org.tctalent.server.model.db.mapper.LanguageMapperImpl;
import org.tctalent.server.model.db.mapper.OccupationMapperImpl;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.EducationLevelService;
import org.tctalent.server.service.db.OccupationService;

class CandidateMapperUnitTest {

  private final CandidateMapper candidateMapper = createCandidateMapper();

  @Test
  void candidateMapAllFields_returnsNull_whenSourceIsNull() {
    assertNull(candidateMapper.candidateMapAllFields(null));
  }

  @Test
  void candidateMapAllFields_mapsEmptyRegistration() {
    Candidate candidate = candidateMapper.candidateMapAllFields(new CandidateRegistration());

    assertNotNull(candidate);
  }


  @Test
  void candidateMapAllFields_mapsSpecificChangedFieldName() {
    CandidateRegistration registration = new CandidateRegistration();
    registration.setContactConsentTcPartners(true);

    Candidate candidate = candidateMapper.candidateMapAllFields(registration);

    assertTrue(candidate.getContactConsentPartners());
  }

  @Test
  void candidateMapAllFields_mapsSpecificOccupationFields() {
    CandidateRegistration registration = new CandidateRegistration();

    org.tctalent.anonymization.model.CandidateOccupation publicCandidateOccupation =
        new org.tctalent.anonymization.model.CandidateOccupation();
    publicCandidateOccupation.setYearsExperience(5);

    org.tctalent.anonymization.model.Occupation occupation =
        new org.tctalent.anonymization.model.Occupation();
    occupation.setName("Accountant");
    occupation.setIsco08Code("2411");

    publicCandidateOccupation.setOccupation(occupation);
    registration.setCandidateOccupations(List.of(publicCandidateOccupation));

    Candidate candidate = candidateMapper.candidateMapAllFields(registration);

    assertNotNull(candidate.getCandidateOccupations());
    assertEquals(1, candidate.getCandidateOccupations().size());

    CandidateOccupation mapped = candidate.getCandidateOccupations().get(0);
    assertEquals(5, mapped.getYearsExperience());
    assertNotNull(mapped.getOccupation());
    assertEquals("2411", mapped.getOccupation().getIsco08Code());
  }

  @Test
  void candidateMapAllFields_mapsSpecificCountryFields() {
    CandidateRegistration registration = new CandidateRegistration();

    org.tctalent.anonymization.model.Country publicCountry =
        new org.tctalent.anonymization.model.Country();
    publicCountry.setName("Country AU");
    publicCountry.setIsoCode("AU");

    registration.setCountry(publicCountry);
    registration.setNationality(publicCountry);

    Candidate candidate = candidateMapper.candidateMapAllFields(registration);

    assertNotNull(candidate.getCountry());
    assertEquals("Country AU", candidate.getCountry().getName());
    assertEquals("AU", candidate.getCountry().getIsoCode());

    assertNotNull(candidate.getNationality());
    assertEquals("Country AU", candidate.getNationality().getName());
    assertEquals("AU", candidate.getNationality().getIsoCode());
  }

  @Test
  void candidateMapAllFields_mapsEducationLevelWithLevel() {
    CandidateRegistration registration = new CandidateRegistration();

    org.tctalent.anonymization.model.EducationLevel publicEducationLevel =
        new org.tctalent.anonymization.model.EducationLevel();
    publicEducationLevel.setLevel(90);

    registration.setMaxEducationLevel(publicEducationLevel);

    Candidate candidate = candidateMapper.candidateMapAllFields(registration);

    assertNotNull(candidate.getMaxEducationLevel());
    assertEquals(90, candidate.getMaxEducationLevel().getLevel());
  }

  @Test
  void candidateMapAllFields_mapsEducationLevelWithoutLevelToNull() {
    CandidateRegistration registration = new CandidateRegistration();

    org.tctalent.anonymization.model.EducationLevel publicEducationLevel =
        new org.tctalent.anonymization.model.EducationLevel();
    publicEducationLevel.setEducationType(EducationType.BACHELOR);
    publicEducationLevel.setLevel(null);

    registration.setMaxEducationLevel(publicEducationLevel);

    Candidate candidate = candidateMapper.candidateMapAllFields(registration);

    assertNull(candidate.getMaxEducationLevel());
  }

  @Test
  void updateCandidateFromSource_returnsWhenSourceIsNull() {
    Candidate target = new Candidate();
    target.setCandidateNumber("target-number");

    candidateMapper.updateCandidateFromSource(null, target);

    assertEquals("target-number", target.getCandidateNumber());
  }

  @Test
  void updateCandidateFromSource_ignoresNullSourceProperties() {
    Candidate source = new Candidate();

    Candidate target = new Candidate();
    ensureSavedListBackingSet(target);
    target.setCandidateNumber("target-number");
    target.setPhone("123");
    target.setWhatsapp("456");

    candidateMapper.updateCandidateFromSource(source, target);

    assertEquals("target-number", target.getCandidateNumber());
    assertEquals("123", target.getPhone());
    assertEquals("456", target.getWhatsapp());
  }


  @Test
  void updateCandidateFromSource_copiesCollectionProperties() {
    Candidate source = new Candidate();

    Occupation occupation = new Occupation();
    occupation.setName("Software Developer");
    occupation.setIsco08Code("2512");

    CandidateOccupation sourceCandidateOccupation = new CandidateOccupation();
    sourceCandidateOccupation.setYearsExperience(7L);
    sourceCandidateOccupation.setOccupation(occupation);

    source.setCandidateOccupations(List.of(sourceCandidateOccupation));

    Candidate target = new Candidate();

    candidateMapper.updateCandidateFromSource(source, target);

    assertNotNull(target.getCandidateOccupations());
    assertEquals(1, target.getCandidateOccupations().size());

    CandidateOccupation mapped = target.getCandidateOccupations().get(0);
    assertEquals(7, mapped.getYearsExperience());
    assertNotNull(mapped.getOccupation());
    assertEquals("Software Developer", mapped.getOccupation().getName());
    assertEquals("2512", mapped.getOccupation().getIsco08Code());
    assertSame(target, mapped.getCandidate());
  }

  @Test
  void updateCandidateFromSource_replacesExistingCollectionProperties() {
    Candidate source = new Candidate();

    CandidateOccupation sourceCandidateOccupation = new CandidateOccupation();
    sourceCandidateOccupation.setYearsExperience(3L);
    source.setCandidateOccupations(List.of(sourceCandidateOccupation));

    Candidate target = new Candidate();

    CandidateOccupation existingCandidateOccupation = new CandidateOccupation();
    existingCandidateOccupation.setYearsExperience(1L);
    target.setCandidateOccupations(new ArrayList<>(List.of(existingCandidateOccupation)));

    candidateMapper.updateCandidateFromSource(source, target);

    assertNotNull(target.getCandidateOccupations());
    assertEquals(1, target.getCandidateOccupations().size());
    assertEquals(3L, target.getCandidateOccupations().get(0).getYearsExperience());
    assertSame(sourceCandidateOccupation, target.getCandidateOccupations().get(0));
  }

  @Test
  void updateCandidateFromSource_doesNotCopyIgnoredFields() {
    Candidate source = new Candidate();
    source.setId(111L);
    source.setCandidateNumber("source-number");

    Map<String, CandidateProperty> sourceProperties = new HashMap<>();
    sourceProperties.put("source", new CandidateProperty());
    source.setCandidateProperties(sourceProperties);

    Candidate target = new Candidate();
    target.setId(222L);
    target.setCandidateNumber("target-number");

    Map<String, CandidateProperty> targetProperties = new HashMap<>();
    targetProperties.put("target", new CandidateProperty());
    target.setCandidateProperties(targetProperties);

    candidateMapper.updateCandidateFromSource(source, target);

    assertEquals(Long.valueOf(222L), target.getId());
    assertSame(targetProperties, target.getCandidateProperties());
    assertTrue(target.getCandidateProperties().containsKey("target"));
    assertEquals("source-number", target.getCandidateNumber());
  }

  @Test
  void updateCandidateFromSource_copiesNonNullPropertiesToExistingTarget() {
    Candidate source = new Candidate();
    source.setCandidateNumber("source-number");
    source.setPhone("999");
    source.setWhatsapp("888");
    source.setAdditionalInfo("source additional info");

    Candidate target = new Candidate();
    target.setCandidateNumber("target-number");
    target.setPhone("123");
    target.setWhatsapp("456");

    candidateMapper.updateCandidateFromSource(source, target);

    assertEquals("source-number", target.getCandidateNumber());
    assertEquals("999", target.getPhone());
    assertEquals("888", target.getWhatsapp());
    assertEquals("source additional info", target.getAdditionalInfo());
  }

  @Test
  void generatedPrivateHelpers_handleNullAndNonNullInputs() {
    CandidateMapperImpl mapper = (CandidateMapperImpl) candidateMapper;

    for (Method method : CandidateMapperImpl.class.getDeclaredMethods()) {
      if (method.getName().equals("candidateMapAllFields")
          || method.getName().equals("updateCandidateFromSource")
          || method.getName().startsWith("set")) {
        continue;
      }

      method.setAccessible(true);

      Object[] nullArgs = new Object[method.getParameterCount()];
      invokeGeneratedHelper(mapper, method, nullArgs);

      Object[] safeNonNullArgs = safeArgumentsFor(method);
      invokeGeneratedHelper(mapper, method, safeNonNullArgs);
    }
  }

  private static void invokeGeneratedHelper(
      CandidateMapperImpl mapper,
      Method method,
      Object[] args
  ) {
    assertDoesNotThrow(() -> {
      try {
        method.invoke(mapper, args);
      } catch (InvocationTargetException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof RuntimeException runtimeException) {
          throw runtimeException;
        }
        if (cause instanceof Error error) {
          throw error;
        }
        throw new RuntimeException(cause);
      }
    }, method.getName());
  }

  private static Object[] safeArgumentsFor(Method method) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    Object[] args = new Object[parameterTypes.length];

    for (int i = 0; i < parameterTypes.length; i++) {
      args[i] = safeValue(parameterTypes[i]);
    }

    return args;
  }

  private static Object safeValue(Type type) {
    if (type instanceof ParameterizedType parameterizedType) {
      return safeParameterizedValue(parameterizedType);
    }

    if (!(type instanceof Class<?> rawType)) {
      return null;
    }

    if (rawType.equals(String.class)) {
      return "sample";
    }
    if (rawType.equals(Boolean.class) || rawType.equals(boolean.class)) {
      return true;
    }
    if (rawType.equals(Integer.class) || rawType.equals(int.class)) {
      return 1;
    }
    if (rawType.equals(Long.class) || rawType.equals(long.class)) {
      return 1L;
    }
    if (rawType.isEnum()) {
      Object[] constants = rawType.getEnumConstants();
      return constants.length == 0 ? null : constants[0];
    }

    if (rawType.isPrimitive()
        || rawType.isInterface()
        || rawType.isArray()
        || rawType.getName().startsWith("java.")) {
      return null;
    }

    return instantiateEmpty(rawType);
  }

  private static Object safeParameterizedValue(ParameterizedType type) {
    Type raw = type.getRawType();
    if (!(raw instanceof Class<?> rawClass)) {
      return null;
    }

    if (List.class.isAssignableFrom(rawClass)) {
      List<Object> values = new ArrayList<>();

      Object element = safeValue(type.getActualTypeArguments()[0]);
      if (element != null) {
        values.add(element);
      }

      return values;
    }

    return null;
  }

  private static Object instantiateEmpty(Class<?> type) {
    try {
      var constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (ReflectiveOperationException | RuntimeException ex) {
      return null;
    }
  }

  private static CandidateMapper createCandidateMapper() {
    CandidateMapperImpl mapper = new CandidateMapperImpl();

    CountryMapperImpl countryMapper = new CountryMapperImpl();
    CountryService countryService = mock(CountryService.class);
    when(countryService.findByIsoCode(anyString())).thenAnswer(invocation -> {
      Country country = new Country();
      country.setIsoCode(invocation.getArgument(0));
      country.setName("Country " + invocation.getArgument(0));
      return country;
    });
    ReflectionTestUtils.setField(countryMapper, "service", countryService);

    EducationLevelMapperImpl educationLevelMapper = new EducationLevelMapperImpl();
    EducationLevelService educationLevelService = mock(EducationLevelService.class);
    when(educationLevelService.findByLevel(anyInt())).thenAnswer(invocation -> {
      EducationLevel educationLevel = new EducationLevel();
      educationLevel.setLevel(invocation.getArgument(0));
      return educationLevel;
    });
    ReflectionTestUtils.setField(educationLevelMapper, "service", educationLevelService);

    OccupationMapperImpl occupationMapper = new OccupationMapperImpl();
    OccupationService occupationService = mock(OccupationService.class);
    when(occupationService.findByIsco08Code(anyString())).thenAnswer(invocation -> {
      Occupation occupation = new Occupation();
      occupation.setIsco08Code(invocation.getArgument(0));
      occupation.setName("Occupation " + invocation.getArgument(0));
      return occupation;
    });
    ReflectionTestUtils.setField(occupationMapper, "service", occupationService);

    setFieldIfPresent(mapper, "countryMapper", countryMapper);
    setFieldIfPresent(mapper, "educationLevelMapper", educationLevelMapper);
    setFieldIfPresent(mapper, "educationMajorMapper", new EducationMajorMapperImpl());
    setFieldIfPresent(mapper, "enumsMapper", new EnumsMapperImpl());
    setFieldIfPresent(mapper, "languageLevelMapper", new LanguageLevelMapperImpl());
    setFieldIfPresent(mapper, "languageMapper", new LanguageMapperImpl());
    setFieldIfPresent(mapper, "occupationMapper", occupationMapper);

    return mapper;
  }

  @Test
  void updateCandidateFromSource_coversNonNullAssignmentsWithNullTargetCollections() {
    Candidate source = new Candidate();
    populateCandidateForUpdate(source);
    source.setCandidateNumber("source-number-null-target-collections");
    source.setPhone("999");
    source.setWhatsapp("888");
    source.setAdditionalInfo("source additional info");

    Candidate target = new Candidate();
    ensureSavedListBackingSet(target);
    target.setCandidateNumber("target-number");

    candidateMapper.updateCandidateFromSource(source, target);

    assertEquals("source-number-null-target-collections", target.getCandidateNumber());
    assertEquals("999", target.getPhone());
    assertEquals("888", target.getWhatsapp());
    assertEquals("source additional info", target.getAdditionalInfo());
  }

  @Test
  void updateCandidateFromSource_coversNonNullAssignmentsWithExistingTargetCollections() {
    Candidate source = new Candidate();
    populateCandidateForUpdate(source);
    source.setCandidateNumber("source-number-existing-target-collections");
    source.setPhone("777");
    source.setWhatsapp("666");
    source.setAdditionalInfo("updated additional info");

    Candidate target = new Candidate();
    populateCandidateForUpdate(target);
    target.setCandidateNumber("target-number");

    candidateMapper.updateCandidateFromSource(source, target);

    assertEquals("source-number-existing-target-collections", target.getCandidateNumber());
    assertEquals("777", target.getPhone());
    assertEquals("666", target.getWhatsapp());
    assertEquals("updated additional info", target.getAdditionalInfo());
  }

  private static boolean setFieldIfPresent(Object target, String fieldName, Object value) {
    try {
      ReflectionTestUtils.setField(target, fieldName, value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  private static <T> T populated(Class<T> type) {
    return type.cast(sampleValue(type, 5));
  }

  private static Object sampleValue(Type type, int depth) {
    if (depth <= 0) {
      return null;
    }

    if (type instanceof ParameterizedType parameterizedType) {
      return sampleParameterizedValue(parameterizedType, depth);
    }

    if (!(type instanceof Class<?> rawType)) {
      return null;
    }

    if (rawType.equals(String.class)) {
      return "sample";
    }
    if (rawType.equals(Boolean.class) || rawType.equals(boolean.class)) {
      return true;
    }
    if (rawType.equals(Integer.class) || rawType.equals(int.class)) {
      return 42;
    }
    if (rawType.equals(Long.class) || rawType.equals(long.class)) {
      return 42L;
    }
    if (rawType.equals(Short.class) || rawType.equals(short.class)) {
      return (short) 4;
    }
    if (rawType.equals(Byte.class) || rawType.equals(byte.class)) {
      return (byte) 2;
    }
    if (rawType.equals(Double.class) || rawType.equals(double.class)) {
      return 42.5d;
    }
    if (rawType.equals(Float.class) || rawType.equals(float.class)) {
      return 42.5f;
    }
    if (rawType.equals(Character.class) || rawType.equals(char.class)) {
      return 'x';
    }
    if (rawType.equals(BigDecimal.class)) {
      return BigDecimal.TEN;
    }
    if (rawType.equals(BigInteger.class)) {
      return BigInteger.TEN;
    }
    if (rawType.equals(LocalDate.class)) {
      return LocalDate.of(2026, 1, 1);
    }
    if (rawType.equals(LocalDateTime.class)) {
      return LocalDateTime.of(2026, 1, 1, 12, 0);
    }
    if (rawType.equals(OffsetDateTime.class)) {
      return OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }
    if (rawType.equals(Instant.class)) {
      return Instant.parse("2026-01-01T12:00:00Z");
    }
    if (rawType.isEnum()) {
      Object[] constants = rawType.getEnumConstants();
      return constants.length == 0 ? null : constants[0];
    }
    if (rawType.isArray()) {
      Object array = Array.newInstance(rawType.getComponentType(), 1);
      Object item = sampleValue(rawType.getComponentType(), depth - 1);
      if (item != null) {
        Array.set(array, 0, item);
      }
      return array;
    }
    if (Collection.class.isAssignableFrom(rawType)) {
      return new ArrayList<>();
    }
    if (Map.class.isAssignableFrom(rawType)) {
      return new LinkedHashMap<>();
    }
    if (rawType.isPrimitive()
        || rawType.isInterface()
        || rawType.getName().startsWith("java.")) {
      return null;
    }

    return instantiateAndPopulate(rawType, depth);
  }

  private static Object sampleParameterizedValue(ParameterizedType type, int depth) {
    Type raw = type.getRawType();
    if (!(raw instanceof Class<?> rawClass)) {
      return null;
    }

    if (Collection.class.isAssignableFrom(rawClass)) {
      Collection<Object> values = new ArrayList<>();
      Type elementType = type.getActualTypeArguments()[0];
      Object value = sampleValue(elementType, depth - 1);
      if (value != null) {
        values.add(value);
      }
      return values;
    }

    if (Map.class.isAssignableFrom(rawClass)) {
      Map<Object, Object> values = new LinkedHashMap<>();
      Object key = sampleValue(type.getActualTypeArguments()[0], depth - 1);
      Object value = sampleValue(type.getActualTypeArguments()[1], depth - 1);
      if (key != null) {
        values.put(key, value);
      }
      return values;
    }

    return sampleValue(rawClass, depth);
  }

  private static Object instantiateAndPopulate(Class<?> type, int depth) {
    Object instance = instantiate(type);
    if (instance == null) {
      return null;
    }

    for (Method method : type.getMethods()) {
      if (!isSetter(method)) {
        continue;
      }

      Object value = sampleValue(method.getGenericParameterTypes()[0], depth - 1);
      if (value == null && method.getParameterTypes()[0].isPrimitive()) {
        continue;
      }

      try {
        method.invoke(instance, value);
      } catch (ReflectiveOperationException | RuntimeException ignored) {
        // Domain setters can reject synthetic values. Leave those fields unset.
      }
    }

    return instance;
  }

  private static Object instantiate(Class<?> type) {
    if (type.isPrimitive()
        || type.isInterface()
        || type.isEnum()
        || type.isArray()
        || type.getName().startsWith("java.")) {
      return null;
    }

    try {
      var constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (ReflectiveOperationException | RuntimeException ex) {
      return null;
    }
  }

  private static boolean isSetter(Method method) {
    return method.getName().startsWith("set")
        && method.getParameterCount() == 1
        && method.getReturnType().equals(Void.TYPE);
  }

  private static void populateCandidateForUpdate(Candidate candidate) {
    ensureSavedListBackingSet(candidate);

    for (Method method : Candidate.class.getMethods()) {
      if (!isCandidateUpdateSetter(method)) {
        continue;
      }

      Object value = valueForUpdateSetter(method.getGenericParameterTypes()[0]);
      if (value == null && method.getParameterTypes()[0].isPrimitive()) {
        continue;
      }

      try {
        method.invoke(candidate, value);
      } catch (ReflectiveOperationException | RuntimeException ignored) {
        // Some domain setters reject synthetic values. Skip those safely.
      }
    }

    ensureSavedListBackingSet(candidate);

    Candidate partnerCandidate = new Candidate();
    ensureSavedListBackingSet(partnerCandidate);
    partnerCandidate.setCandidateNumber("partner-candidate-number");
    candidate.setPartnerCandidate(partnerCandidate);
  }

  private static boolean isCandidateUpdateSetter(Method method) {
    if (!method.getName().startsWith("set")
        || method.getParameterCount() != 1
        || !method.getReturnType().equals(Void.TYPE)) {
      return false;
    }

    return !Set.of(
        "setId",
        "setCandidateProperties",
        "setSavedLists",
        "setPartnerCandidate"
    ).contains(method.getName());
  }

  private static Object valueForUpdateSetter(Type type) {
    if (type instanceof ParameterizedType parameterizedType) {
      return valueForParameterizedUpdateSetter(parameterizedType);
    }

    if (!(type instanceof Class<?> rawType)) {
      return null;
    }

    if (rawType.equals(String.class)) {
      return "sample";
    }
    if (rawType.equals(Boolean.class) || rawType.equals(boolean.class)) {
      return true;
    }
    if (rawType.equals(Integer.class) || rawType.equals(int.class)) {
      return 42;
    }
    if (rawType.equals(Long.class) || rawType.equals(long.class)) {
      return 42L;
    }
    if (rawType.equals(Short.class) || rawType.equals(short.class)) {
      return (short) 4;
    }
    if (rawType.equals(Byte.class) || rawType.equals(byte.class)) {
      return (byte) 2;
    }
    if (rawType.equals(Double.class) || rawType.equals(double.class)) {
      return 42.5d;
    }
    if (rawType.equals(Float.class) || rawType.equals(float.class)) {
      return 42.5f;
    }
    if (rawType.equals(Character.class) || rawType.equals(char.class)) {
      return 'x';
    }
    if (rawType.equals(BigDecimal.class)) {
      return BigDecimal.TEN;
    }
    if (rawType.equals(BigInteger.class)) {
      return BigInteger.TEN;
    }
    if (rawType.equals(LocalDate.class)) {
      return LocalDate.of(2026, 1, 1);
    }
    if (rawType.equals(LocalDateTime.class)) {
      return LocalDateTime.of(2026, 1, 1, 12, 0);
    }
    if (rawType.equals(OffsetDateTime.class)) {
      return OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }
    if (rawType.equals(Instant.class)) {
      return Instant.parse("2026-01-01T12:00:00Z");
    }
    if (rawType.equals(Number.class)) {
      return 42;
    }
    if (rawType.isEnum()) {
      Object[] constants = rawType.getEnumConstants();
      return constants.length == 0 ? null : constants[0];
    }
    if (List.class.isAssignableFrom(rawType)) {
      return new ArrayList<>();
    }
    if (Set.class.isAssignableFrom(rawType)) {
      return new LinkedHashSet<>();
    }
    if (Map.class.isAssignableFrom(rawType)) {
      return new LinkedHashMap<>();
    }
    if (rawType.isPrimitive()
        || rawType.isInterface()
        || rawType.isArray()
        || rawType.getName().startsWith("java.")) {
      return null;
    }

    return instantiateEmpty(rawType);
  }

  private static Object valueForParameterizedUpdateSetter(ParameterizedType type) {
    Type raw = type.getRawType();
    if (!(raw instanceof Class<?> rawClass)) {
      return null;
    }

    if (List.class.isAssignableFrom(rawClass)) {
      return new ArrayList<>();
    }

    if (Set.class.isAssignableFrom(rawClass)) {
      return new LinkedHashSet<>();
    }

    if (Map.class.isAssignableFrom(rawClass)) {
      return new LinkedHashMap<>();
    }

    return valueForUpdateSetter(rawClass);
  }

  private static void ensureSavedListBackingSet(Candidate candidate) {
    ReflectionTestUtils.setField(candidate, "candidateSavedLists", new LinkedHashSet<CandidateSavedList>());
  }
}