package msocket.impl

import scala.annotation.switch
import scala.scalajs.js.typedarray.Uint8Array

object JsonObjectParser {

  final val SquareBraceStart = '['.toByte
  final val SquareBraceEnd   = ']'.toByte
  final val CurlyBraceStart  = '{'.toByte
  final val CurlyBraceEnd    = '}'.toByte
  final val DoubleQuote      = '"'.toByte
  final val Backslash        = '\\'.toByte
  final val Comma            = ','.toByte

  final val LineBreak  = 10 // '\n'
  final val LineBreak2 = 13 // '\r'
  final val Tab        = 9  // '\t'
  final val Space      = 32 // ' '

  def isWhitespace(b: Byte): Boolean = (b: @switch) match {
    case Space      => true
    case LineBreak  => true
    case LineBreak2 => true
    case Tab        => true
    case _          => false
  }

}

/**
 * **Mutable** framing implementation that given any number of [[Uint8Array]] chunks, can emit JSON objects contained within them.
 * Typically JSON objects are separated by new-lines or commas, however a top-level JSON Array can also be understood and chunked up
 * into valid JSON objects by this framing implementation.
 *
 * Leading whitespace between elements will be trimmed.
 */
class JsonObjectParser(maximumObjectLength: Int = Int.MaxValue) {
  import JsonObjectParser._

  private var buffer: Uint8Array = new Uint8Array(0)

  private var pos       = 0 // latest position of pointer while scanning for json object end
  private var trimFront = 0 // number of chars to drop from the front of the bytestring before emitting (skip whitespace etc)
  private var depth     = 0 // counter of object-nesting depth, once hits 0 an object should be emitted

  private var completedObject         = false
  private var inStringExpression      = false
  private var isStartOfEscapeSequence = false
  private var lastInput               = 0.toByte

  /**
   * Appends input Uint8Array to internal byte string buffer.
   * Use [[poll]] to extract contained JSON objects.
   */
  def offer(input: Uint8Array): Unit = {
    val array = new Uint8Array(buffer.length + input.length)
    array.set(buffer)
    array.set(input, buffer.length)
    buffer = array
  }

  def isEmpty: Boolean = buffer.isEmpty

  /**
   * Attempt to locate next complete JSON object in buffered Uint8Array and returns `Some(it)` if found.
   * May throw RuntimeException if the contained JSON is invalid or max object size is exceeded.
   */
  def poll(): Option[Uint8Array] = {
    val foundObject = seekObject()
    if (!foundObject) None
    else
      (pos: @switch) match {
        case -1 | 0 => None
        case _ =>
          val emit = buffer.subarray(0, pos)
          buffer = buffer.subarray(pos)
          pos = 0

          val tf = trimFront
          trimFront = 0

          if (tf == 0) Some(emit)
          else {
            val trimmed = emit.subarray(tf, emit.length)
            if (trimmed.isEmpty) None
            else Some(trimmed)
          }
      }
  }

  /** @return true if an entire valid JSON object was found, false otherwise */
  private def seekObject(): Boolean = {
    completedObject = false
    val bufSize = buffer.size
    while (pos != -1 && (pos < bufSize && pos < maximumObjectLength) && !completedObject) proceed(buffer(pos).byteValue)

    if (pos >= maximumObjectLength)
      throw new RuntimeException(s"""JSON element exceeded maximumObjectLength ($maximumObjectLength bytes)!""")

    completedObject
  }

  private def proceed(input: Byte): Unit = {
    if (input == SquareBraceStart && outsideObject) {
      // outer object is an array
      pos += 1
      trimFront += 1
    } else if (input == SquareBraceEnd && outsideObject) {
      // outer array completed!
      pos = -1
    } else if (input == Comma && outsideObject) {
      // do nothing
      pos += 1
      trimFront += 1
    } else if (input == Backslash) {
      if (lastInput == Backslash & isStartOfEscapeSequence) isStartOfEscapeSequence = false
      else isStartOfEscapeSequence = true
      pos += 1
    } else if (input == DoubleQuote) {
      if (!isStartOfEscapeSequence) inStringExpression = !inStringExpression
      isStartOfEscapeSequence = false
      pos += 1
    } else if (input == CurlyBraceStart && !inStringExpression) {
      isStartOfEscapeSequence = false
      depth += 1
      pos += 1
    } else if (input == CurlyBraceEnd && !inStringExpression) {
      isStartOfEscapeSequence = false
      depth -= 1
      pos += 1
      if (depth == 0) {
        completedObject = true
      }
    } else if (isWhitespace(input) && !inStringExpression) {
      pos += 1
      if (depth == 0) trimFront += 1
    } else if (insideObject) {
      isStartOfEscapeSequence = false
      pos += 1
    } else {
      throw new RuntimeException(s"Invalid JSON encountered at position [$pos] of [$buffer]")
    }

    lastInput = input
  }

  @inline private final def insideObject: Boolean =
    !outsideObject

  @inline private final def outsideObject: Boolean =
    depth == 0

}
