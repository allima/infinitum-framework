**Contents**


# Introduction #

The `OneToMany` annotation is simply the converse of ManyToOne and is used to define a one-to-many relationship between two domain entities. The annotation is placed in the "one" side of the relationship on the field which models it, while a `ManyToOne` annotation is placed in the "many" side of the relationship. For example, if class `Foo` has a one-to-many relationship with class `Bar`, `Foo` will contain a collection of `Bar` and Bar will contain a `Foo` field. The `OneToMany` annotation would then be placed on the field in `Foo` and the `ManyToOne` annotation on the field in `Bar`.

# Annotation Attributes #

`OneToMany` has three required attributes that are used to define the relationship and how it should be represented.

**className:** identifies the package-qualified name of the persistent class this relationship links to.

**column:** indicates the name of the column representing the foreign key in this relationship.

**name:**  indicates the name of the relationship. Names are used to uniquely identify relationships within a class and provide descriptive metadata.

# `OneToMany` Example #

The code below shows how a one-to-many relationship is established between the two classes `Department` and `Manager` using the `OneToMany` and `ManyToOne` annotations. Let's assume that a `Department` can be managed by multiple `Manager`s and a `Manager` can manage one `Department`.

```
public class Department {

    private String mName;

    @OneToMany(name="managedBy", className="com.example.domain.Manager", column="dept")
    private List<Manager> mManagers;

    // ...

}

public class Manager extends Employee {

    @ManyToOne(name="manages", className="com.example.domain.Department", column="dept")
    private Department mDepartment;

    // ...

}
```

The example above establishes a one-to-many relationship between `Department` and `Manager`. The column `dept` in the `Manager` table acts as a foreign key referencing the `Department` a `Manager` is responsible for.