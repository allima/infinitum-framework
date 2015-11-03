**Contents**


# Introduction #

As an alternative to inline Java annotations, domain model metadata can be provided through Infinitum XML mappings known as imf.xml files. Each persistent class has an imf.xml file associated with it, and, like InfinitumCfgXml, these files are stored in res/raw. For example, a class `Foo` would have a map file `foo.imf.xml`.

# Class-level Configuration #

There are a number of different class-level properties that can be applied to persistent entities. The root element of an imf.xml file, ignoring `infinitum-mapping`, is the `class` node, which contains the metadata for the class itself as well as its fields. Classes can be configured to lazily load and cascade their associations using the `lazy` and `cascade` attributes respectively. Note that these properties are enabled by default, so they need not be explicitly defined. By default, domain entities are persisted to a table or REST endpoint of the same name, but both of these properties can be changed by specifying the `table` and `rest` attributes.

## Class-level Configuration Example ##

```
<class name="com.example.domain.foo" lazy="false" table="my_table">

    ...

</class>
```

# Field-level Configuration #

Each persistent field needs to be present in an imf.xml file. A normal field, meaning a field which is not part of a relationship with another domain entity and not a primary key, is represented using the `property` element. A field is, by default, mapped to a column with the same name. If Android naming conventions are being followed, specifically member variable names are prefixed with a lowercase 'm' (i.e. `private int mFoo`), the prefix will be dropped. The column can be explicitly defined using the `column` attribute. A field can also be specified as not nullable or unique using the `not-null` and `unique` attributes respectively. Similar to the column, a member variable is mapped to a RESTful field of the same name unless otherwise defined using the `rest` attribute.

Primary keys are defined using the `primary-key` element, which included attributes for specifying the column and autoincrement value.

Entity relationships are defined using the `many-to-many`, `many-to-one`, `one-to-many`, and `one-to-one` elements. The `many-to-many` element includes attributes for specifying the many-to-many table and the entity fields that are part of the relationship on either side. The remaining relationship elements have an attribute to specify the foreign key used to establish the association.

Note that, when dealing with persistent class hierarchies, each class in the hierarchy needs an imf.xml file.

## Field-level Configuration Example ##

```
<class name="com.example.domain.foo">
    <primary-key name="mId" type="long" autoincrement="true" />
    <property name="mFoo" type="String" />
    <property name="mBar" type="int" column="my_col" not-null="true" />
    <property name="mBaz" type="Date" unique="true" />
    <many-to-many name="foo-bam" field="mBam"
        class="com.example.domain.Bam" foreign-field="mId" key-field="mId" table="foo_bam" />
    <many-to-one name="foo-qux" field="mQux"
        class="com.example.domain.Qux" column="qux" />
</class>
```

# Map File Example #

The example below shows a class `Foo` and its associated map file `foo.imf.xml`. Note that `Foo` has a many-to-one relationship with the class `Bar` and a many-to-many relationship with the class `Baz`.

**Foo.java**

```
public class Foo {

    private long mId;
    private String mFoo;
    private Bar mBar;
    private List<Baz> mBaz;

    public Foo() {
        mBaz = new ArrayList<Baz>();
    }

    // ...

}
```

**foo.imf.xml**

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE infinitum-mapping PUBLIC
"-//Infinitum/Infinitum Mapping DTD 1.0//EN"
"http://clarionmedia.com/infinitum/dtd/infinitum-mapping-1.0.dtd">

<infinitum-mapping>
    <class name="com.example.domain.Foo" lazy="true" cascade="true">
        <primary-key name="mId" type="long" autoincrement="true" />
        <property name="mFoo" type="String" />
        <many-to-many name="foo-baz" field="mBaz"
            class="com.example.domain.Baz" foreign-field="mId" key-field="mId" table="foo_baz" />
        <many-to-one name="foo-bar" field="mBar"
            class="com.example.domain.Bar" column="bar" />
    </class>
</infinitum-mapping>
```