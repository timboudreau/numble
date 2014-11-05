Numble
======

This is a small framework-agnostic library that allows you to generate entity classes from an
annotation, generate instances from data, and validate the input data they're
created from.

Basically, boilerplate getters and setters, marshalling and validation
code are no fun to write, and many frameworks force you to mix that sort of thing
into logic that does other things, which becomes hard to maintain.

This library lets you specify, declaratively, what your input looks like, and it
will generate a correctly implemented class to provide validation, marshalling
and type-safe access to it.  It's common to deal in untyped data that consists
of hashes of strings;  this library automatically generates a typesafe class for
that data.

The idea is to integrate it this project into frameworks - so if you are, say,
supplying a constructor argument of a generated type, the framework validates
the data (and does the Right Thing&trade; if the data is invalid) and then
hands your code a beautiful, valid, typesafe object that represents the parameters
you expect.

So, you annotate a class like so:

```java
@Params(value = {
    @Param(value = "optionalSomething", type = Types.NON_EMPTY_STRING, required = false),
    @Param(value = "requiredInt", type = Types.INTEGER),
    @Param(value = "requiredBool", type = Types.BOOLEAN, defaultValue = "false"),
    @Param(value = "requiredNonNeg", type = Types.NON_NEGATIVE_INTEGER),
    @Param(value = "requiredNumber", type = Types.DOUBLE, defaultValue = "23"),
    @Param(value = "nothing", defaultValue = "Go away", required = false, 
           constraints = {StringValidators.MAY_NOT_END_WITH_PERIOD, 
                          StringValidators.MAY_NOT_START_WITH_DIGIT}),
    @Param(value = "defaultInt", type = Types.INTEGER, defaultValue = "5"),
    @Param(value = "jthing", type = Types.STRING, required = false, 
        validators = {LongerThanTwo.class, StartsWithJValidator.class})}
        ,allowUnlistedParameters = true
        ,generateToJSON = true
        ,generateValidationCode = true)
```

and this generates, in the same package, an immutable class which has getters for all of
these parameters, using `Optional` for those properties which are not required and do
not have a default value.

The resulting class can be instantiated either via dependency injection (say, with Guice),
or via deserialization using [Jackson](https://github.com/FasterXML/jackson) - it will have
one constructor with Jackson's annotations, and one annotated with `@Inject` which takes
an instance of `KeysValues` (defined here - basically a map - bind it in your dependency
injection framework for whatever works for you - this keeps us framework-agnostic).

The goal was to create a framework for interpreting URL parameters and request bodies that
could be used with both [Acteur](https://timboudreau.com/blog/updatedActeur/read),
[Wicket](https://wicket.apache.org/) and any similar framework that ingests key-value or
JSON data.  For example, Acteur has the `@InjectRequestBodyAs` annotation, and soon
a similar one for URL parameters.  The goal was to eliminate manual parameter validation
and type coercion code from classes whose job is the logic of handling a request, and
which should be focused on that.

The generated class will be named `$NAME_OF_CLASS_WITH_THE_ANNOTATION + "Params"`.


Validation
----------

This project leverages a validation framework, [SimpleValidation](https://kenai.com/projects/simplevalidation) - ([javadoc here](http://timboudreau.com/builds/job/SimpleValidation/lastSuccessfulBuild/artifact/ValidationAPI/target/apidocs/index.html))
to validate input data.  Each parameter has two values that can list validators
(a validator simply takes some input and either passes it or adds a localized error
message to a list of problems):

 * `validators` - a list of validator classes.  They will be instantiated using Guice, so
if they need to take some additional objects in their constructor, that is fine
 * `constraints` - a list of validators defined in the [StringValidators](http://timboudreau.com/builds/job/SimpleValidation/lastSuccessfulBuild/artifact/ValidationAPI/target/apidocs/org/netbeans/validation/api/builtin/stringvalidation/StringValidators.html) enum, which consists of a lot of predefined validators for URLs, email addreses and more.

It is preferable to perform validation *before* instantiating an object, so it
is simply impossible for an object with invalid to exist.  However, generated objects
can be generated with a `validate()` method that will run validation post-hoc.
The `ParamChecker` class can be used to pre-validate data.

Usage
-----

The thing to remember is that you want to validate your data *before* you instantiate
the object, if at all possible.  So, typically, you know you are *going* to create
an object from some data.  The class that can do pre-validation is `ParamChecker` - 
create one, create a [Problems](http://timboudreau.com/builds/job/SimpleValidation/lastSuccessfulBuild/artifact/ValidationAPI/target/apidocs/org/netbeans/validation/api/Problems.html)
and pass your `KeysValues` to it first, if you're using injection.  If you're using
JSON, an option is to load your data as a Map first, create a `KeysValues.MapAdapter` over
that.

Failing that, you can instantiate your object using Jackson and (assuming `generateValidationCode() == true`)
call the `validate()` method of the resulting object to validate the object after-the-fact.

#### Why Not Have Objects Throw An Exception In Their Constructor

It's generally not very nice to do that - in particular, Guice frowns upon that.
So, generally, validate your data *before* instantiating your object, and that
way you're guaranteed never to have an instance of one of your types that is
not valid.

Generated Classes
-----------------

The generated classes will have:

 - A constructor annotated with `@Inject` which takes a `KeysValues` (trivial to wrap a Map or similar in this)
 - Final fields + getters for all specified parameters, using `Optional` for those that could be null
 - Correct implementation of `equals()` and `hashCode()` and a meaningful `toString()` implementation
 - A `toMap()` method which converts the object to a `Map<String,Object>`
 - (optional) A constructor using Jackson's annotations for instantiation from JSON
 - (optional) A `toJSON()` method that converts the object back to JSON (if you do this directly, you'll need to
configure Jackson to understand `Optional`, which it doesn't by default)
 - (optional) A `validate()` method for checking the correctness of that data post-instantiation
 - (optional) If `allowUnlistedParameters()` is true, the generated class will contain a `get(String)` method that returns `Optional<String>`, and an internal `__any(String,String)` method which will be annotated with `@JsonAnySetter` if `jsonConstructor()` returns true, so that properties that are not explicitly specified are captured

Notes
-----

Why not use [Lombok](http://projectlombok.org/) / other-random-library-like-this?

I have fairly strong feelings about making data immutable, and the whole point here is
that you shouldn't have to manually define the class at all.  Plus, it was fun to
write.


Generated Class Example
-----------------------

This is what is generated from the annotations above:

```java
@Origin(com.mastfrog.parameters.FakePage.class)
public final class FakePageParams implements Serializable, Validatable {
    private final int _defaultInt;
    private final Optional<String> _jthing;
    private final String _nothing;
    private final Optional<String> _optionalSomething;
    private final boolean _requiredBool;
    private final int _requiredInt;
    private final int _requiredNonNeg;
    private final double _requiredNumber;
    private final Map<String,String> __metadata = new HashMap<>();

    @Inject
    public FakePageParams (KeysValues params) {
        this._defaultInt = params.get("defaultInt") == null ? 5 : Integer.parseInt(params.get("defaultInt"));
        this._jthing = Optional.ofNullable(params.get("jthing") == null ? null : params.get("jthing"));
        this._nothing = params.get("nothing") == null ? "Go away" : params.get("nothing");
        this._optionalSomething = Optional.ofNullable(params.get("optionalSomething") == null ? null : params.get("optionalSomething"));
        this._requiredBool = params.get("requiredBool") == null ? false : Boolean.parseBoolean(params.get("requiredBool"));
        this._requiredInt = Integer.parseInt(params.get("requiredInt"));
        this._requiredNonNeg = Integer.parseInt(params.get("requiredNonNeg"));
        this._requiredNumber = params.get("requiredNumber") == null ? 23 : Double.parseDouble(params.get("requiredNumber"));
        for (Map.Entry<String,String> __e : params) {
            switch (__e.getKey()) {
                case "defaultInt" :
                case "jthing" :
                case "nothing" :
                case "optionalSomething" :
                case "requiredBool" :
                case "requiredInt" :
                case "requiredNonNeg" :
                case "requiredNumber" :
                    break;
                default :
                    __any (__e.getKey(), __e.getValue());
            }
        }
    }

    @JsonCreator
    public FakePageParams(
        @JsonProperty(value="defaultInt", required=false) Integer _defaultInt,
        @JsonProperty(value="jthing", required=false) String _jthing,
        @JsonProperty(value="nothing", required=false) String _nothing,
        @JsonProperty(value="optionalSomething", required=false) String _optionalSomething,
        @JsonProperty(value="requiredBool", required=false) Boolean _requiredBool,
        @JsonProperty(value="requiredInt") int _requiredInt,
        @JsonProperty(value="requiredNonNeg") int _requiredNonNeg,
        @JsonProperty(value="requiredNumber", required=false) Double _requiredNumber) {
        this._defaultInt = _defaultInt == null ? 5 : _defaultInt;
        this._jthing = Optional.ofNullable(_jthing);
        this._nothing = _nothing == null ? "Go away" : _nothing;
        this._optionalSomething = Optional.ofNullable(_optionalSomething);
        this._requiredBool = _requiredBool == null ? false : _requiredBool;
        this._requiredInt = _requiredInt;
        this._requiredNonNeg = _requiredNonNeg;
        this._requiredNumber = _requiredNumber == null ? 23 : _requiredNumber;
    }


    @JsonAnySetter
    public void __any(String key, String value){
        __metadata.put(key, value);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(__metadata.get(key));
    }

    public int getDefaultInt() {
        return _defaultInt;
    }

    public Optional<String> getJthing() {
        return _jthing;
    }

    public String getNothing() {
        return _nothing;
    }

    public Optional<String> getOptionalSomething() {
        return _optionalSomething;
    }

    public boolean getRequiredBool() {
        return _requiredBool;
    }

    public int getRequiredInt() {
        return _requiredInt;
    }

    public int getRequiredNonNeg() {
        return _requiredNonNeg;
    }

    public double getRequiredNumber() {
        return _requiredNumber;
    }

    @Override
    public String toString() {
        return 
           " defaultInt = " + _defaultInt
            + " jthing = " + _jthing
            + " nothing = " + _nothing
            + " optionalSomething = " + _optionalSomething
            + " requiredBool = " + _requiredBool
            + " requiredInt = " + _requiredInt
            + " requiredNonNeg = " + _requiredNonNeg
            + " requiredNumber = " + _requiredNumber;
    }

    @Override
    public boolean equals (Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        }

        if (o instanceof FakePageParams) {
            FakePageParams other = (FakePageParams) o;
            return 
                this._defaultInt == other._defaultInt &&
                Objects.equals(this._jthing, other._jthing)  &&
                Objects.equals(this._nothing, other._nothing)  &&
                Objects.equals(this._optionalSomething, other._optionalSomething)  &&
                Objects.equals(this._requiredBool, other._requiredBool)  &&
                this._requiredInt == other._requiredInt &&
                this._requiredNonNeg == other._requiredNonNeg &&
                this._requiredNumber == other._requiredNumber;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            _defaultInt,
            _jthing,
            _nothing,
            _optionalSomething,
            _requiredBool,
            _requiredInt,
            _requiredNonNeg,
            _requiredNumber);
    }

    @Override
    public Problems validate (Injector inj, Problems problems) {
        if (_jthing.isPresent()) {
            Validator<String>  _jthingValidator1 = inj.getInstance(com.mastfrog.parameters.LongerThanTwo.class);
            _jthingValidator1.validate (problems, "jthing", _jthing.get() );
        }
        if (_jthing.isPresent()) {
            Validator<String>  _jthingValidator2 = inj.getInstance(com.mastfrog.parameters.StartsWithJValidator.class);
            _jthingValidator2.validate (problems, "jthing", _jthing.get() );
        }
        MAY_NOT_END_WITH_PERIOD.validate(problems, "nothing", _nothing);
        MAY_NOT_START_WITH_DIGIT.validate(problems, "nothing", _nothing);
        return problems;
    }

    public Map<String,Object> toMap() {
        Map<String,Object> result = new HashMap<>();
        result.put("defaultInt", _defaultInt);
        if (_jthing.isPresent()) {
            result.put("jthing", _jthing.get());
        }
        result.put("nothing", _nothing);
        if (_optionalSomething.isPresent()) {
            result.put("optionalSomething", _optionalSomething.get());
        }
        result.put("requiredBool", _requiredBool);
        result.put("requiredInt", _requiredInt);
        result.put("requiredNonNeg", _requiredNonNeg);
        result.put("requiredNumber", _requiredNumber);
        result.putAll(__metadata);
        return result;
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(toMap());
    }
}
```
