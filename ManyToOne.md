**Contents**


# Introduction #

The `ManyToOne` annotation is used to define a many-to-one relationship between two domain entities. The annotation is placed in the "many" side of the relationship on the field which models it. Conversely, a OneToMany annotation is placed in the "one" side of the relationship. For example, if class `Foo` has a many-to-one relationship with class `Bar`, `Foo` will contain a `Bar` field and `Bar` will contain a collection of `Foo`. The `ManyToOne` annotation would then be placed on the field in `Foo` and the `OneToMany` annotation on the field in `Bar`.

# Annotation Attributes #

`ManyToOne` has three required attributes that are used to define the relationship and how it should be represented.

**className:** identifies the package-qualified name of the persistent class this relationship links to.

**column:** indicates the name of the column representing the foreign key in this relationship.

**name:**  indicates the name of the relationship. Names are used to uniquely identify relationships within a class and provide descriptive metadata.

# `ManyToOne` Example #

The code below shows how a many-to-one relationship is established between the two classes `Manager` and `Department` using the `ManyToOne` and `OneToMany` annotations. Let's assume that a `Manager` can manage one `Department` and a `Department` can be managed by multiple `Manager`s.

```
public class Manager extends Employee {

    @ManyToOne(name="manages", className="com.example.domain.Department", column="dept")
    private Department mDepartment;

    // ...

}

public class Department {

    private String mName;

    @OneToMany(name="managedBy", className="com.example.domain.Manager", column="dept")
    private List<Manager> mManagers;

    // ...

}
```

The example above establishes a many-to-one relationship between `Manager` and `Department`. The column `dept` in the `Manager` table acts as a foreign key referencing the `Department` a `Manager` is responsible for.