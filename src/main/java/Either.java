class Either {
  interface EitherVisitor<A, B, C> {
    public C visitLeft(Left<A, B> left);
    public C visitRight(Right<A, B> right);
  }

  interface Either<A, B> {
    public <C> C accept(EitherVisitor<A, B, C> visitor);
  }

  public static final class Left<A, B> implements Either<A, B> {
    public final A value;
    public Left(A value) {
      this.value = value;
    }

    public <C> C accept(EitherVisitor<A, B, C> visitor) {
      return visitor.visitLeft(this);
    }
  }

  public static final class Right<A, B> implements Either<A, B> {
    public final B value;
    public Right(B value) {
      this.value = value;
    }

    public <C> C accept(EitherVisitor<A, B, C> visitor) {
      return visitor.visitRight(this);
    }
  }
}
