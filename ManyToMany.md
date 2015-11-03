**Contents**


# Introduction #

The `ManyToMany` annotation is used to define a many-to-many relationship between two domain entities. The annotation is placed in each class which make up the relationship on the field which models it. For example, if class `Foo` has a many-to-many relationship with class `Bar`, `Foo` will contain a collection of `Bar` and vice versa. The annotation would then be placed on each of these fields in the respective classes.

# Annotation Attributes #

`ManyToMany` has five required attributes that are used to define the relationship and how it should be stored.

**className:** identifies the package-qualified name of the persistent class this relationship links to.

**tableName:** indicates the name of the many-to-many table which this relationship will be stored in.

**keyField:** indicates the name of the field identifying this class's side (relative to the annotation) of the relationship, typically its primary key.

**foreignField:** indicates the name of the field identifying the associated class's side (relative to the annotation) of the relationship, typically the primary key of the associated class.

**name:** indicates the name of the relationship. Names are used to uniquely identify relationships within a class and provide descriptive metadata.

# `ManyToMany` Example #

The code below shows how a many-to-many relationship is established between the two classes `Employee` and `Department` using the `ManyToMany` annotation. Let's assume that an `Employee` can work in multiple `Department`s and a `Department` has multiple `Employee`s. Let's also assume that an `Employee` is uniquely identified by his or her employee ID and a `Department` is uniquely identified by its name.

```
public class Employee {

    private long mEmpId;

    @ManyToMany(name="worksIn", className="com.example.domain.Department", keyField="mEmpId", foreignField="mName", tableName="emp_dept")
    private List<Department> mDepartments;

    // ...

}

public class Department {
    
    @PrimaryKey(autoincrement=false)
    private String mName;

    @ManyToMany(name="employs", className="com.example.domain.Employee", keyField="mName", foreignField="mEmpId", tableName="emp_dept")
    private List<Employee> mEmployees;

    // ...

}
```

A many-to-many relationship has now been identified between `Employee` and `Department` such that the relationship data will be stored in the table `emp_dept`.