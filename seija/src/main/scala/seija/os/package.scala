package seija

package object os {

    val root:Path = new Path(new RawPath(Foreign.root))

    val home:Path = new Path(new RawPath(Foreign.home))

    val pwd:Path = new Path(new RawPath(Foreign.pwd))

    val rel = RelPath.rel
}
