**Contents**


# Introduction #

The `OneToOne` annotation is used to define a one-to-one relationship between two domain entities. The annotation is placed in each class which make up the relationship on the field which models it. For example, if class `Foo` has a one-to-one relationship with class `Bar`, `Foo` will contain a `Bar` field and vice versa. The annotation would then be placed on each of these fields in the respective classes.

# Annotation Attributes #

`OneToOne` has three required attributes that are used to define the relationship and how it should be represented.

**className:** identifies the package-qualified name of the persistent class this relationship links to.

**column:** indicates the name of the column representing the foreign key in this relationship. This column needs to be non-null and unique to maintain one-to-one integrity.

**name:** indicates the name of the relationship. Names are used to uniquely identify relationships within a class and provide descriptive metadata.

# `OneToOne` Example #

The code below shows how a one-to-one relationship is established between the two classes `Manager` and `Department`. Let's assume that a `Manager` manages exactly one `Department` and a `Department` has exactly one `Manager`.

```
public class Manager extends Employee {

    @OneToOne(name="manages", className="com.example.domain.Department", column="dept")
    private Department mDepartment;

    // ...

}

public class Department {

    private String mName;

    @OneToOne(name="managedBy", className="com.example.domain.Manager", column="dept")
    private Manager mManager;

    // ...

}
```

The example above establishes a one-to-one relationship between `Manager` and `Department`. The column `dept` in the `Manager` table acts as a foreign key referencing the `Department` a `Manager` is responsible for. Since this is a one-to-one relationship, this column is unique and not nullable.