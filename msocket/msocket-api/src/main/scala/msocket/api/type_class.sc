trait Show[A] {
  def show(a: A): String
}

object Show {
  val intCanShow: Show[Int] = {
    (a: Int) => s"int: ${a}"
  }
}
