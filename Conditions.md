**Contents**


# Introduction #

`Conditions` contains static factory methods for creating [Criterion](Criterion.md). The full list of `Criterion` provided by `Conditions` is given below.

# Conditions Criterion #

**Equals**: places an "equals" condition on the class field with the given name.
```
criteria.add(Conditions.eq("mId", 42));
```

**Not Equal**: places a "not equal" condition on the class field with the given name.
```
criteria.add(Conditions.ne("mId", 42));
```

**Greater Than**: places a "greater than" condition on the class field with the given name.
```
criteria.add(Conditions.gt("mId", 42));
```

**Less Than**: places a "less than" condition on the class field with the given name.
```
criteria.add(Conditions.lt("mId", 42));
```

**Greater Than or Equal To**: places a "greater than or equal to" condition on the class field with the given name.
```
criteria.add(Conditions.gte("mId", 42));
```

**Less Than or Equal To**: places a "less than or equal to" condition on the class field with the given name.
```
criteria.add(Conditions.lte("mId", 42));
```

**Between**: places a "between" condition on the class field with the given name.
```
criteria.add(Conditions.between("mId", 42, 100));
```

**In**: places an "in" condition on the class field with the given name.
```
criteria.add(Conditions.in("mId", new Object[]{42, 56, 98}));
```

**Like**: places a "like" condition on the class field with the given name.
```
criteria.add(Conditions.like("mBar", "hello%"));
```

**Is Null**: places an "is null" condition on the class field with the given name.
```
criteria.add(Conditions.isNull("mBar"));
```

**Is Not Null**: places an "is not null" condition on the class field with the given name.
```
criteria.add(Conditions.isNotNull("mBar"));
```

**And**: places a restriction consisting of the conjunction of two `Criterion`.
```
criteria.add(Conditions.and(Conditions.eq("mId", 42), Conditions.like("mBar", "hello%")));
```

**Or**: places a restriction consisting of the disjunction of two `Criterion`.
```
criteria.add(Conditions.or(Conditions.eq("mId", 42), Conditions.like("mBar", "hello%")));
```

**Not**: places a restriction consisting of the negation of the given `Criterion`.
```
criteria.add(Conditions.not(Conditions.like("mBar", "hello%")));
```