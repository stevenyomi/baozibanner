from base64 import b64encode

with open('banner.jpg', 'rb') as f:
    data = f.read()

head = b'''\
package com.github.stevenyomi.baozibanner

val BANNER_BASE64 get() = "\
'''

tail = b'''\
"
const val BANNER_WIDTH = 800
const val BANNER_HEIGHT = 282
'''

with open('../library/src/main/kotlin/com/github/stevenyomi/baozibanner/BannerData.kt', 'wb') as f:
    f.write(head)
    f.write(b64encode(data))
    f.write(tail)
