# ImageLoader
ImageLoader in Android-art-res

1. 优化图片加载过程，将加载过的图片根据采样率压缩后缓存到LruCache，并将原图缓存到DiskLruCache中，对于磁盘读取图片和网络读取图片，使用了线程池来并发读取
再由Handler设置到ImageView中
2. 解决了列表错位问题，在设置图片之前会根据url来判断item是否发生了复用，如果发生了复用就不设置图片
3. 为了避免滑动时产生太多线程，所以监听了GrdView的滑动，当滑动停止时才加载图片

## 9/26更新
1. 之前书中对复用的item中的imageView用占位图替换，此次将其更改为优先读取内存中缓存的缩略图，如果没有缓存才使用占位图
2. 扩大了内存缓存的容量，使其能够缓存全部缩略图
3. 增加大图功能，显示大图时会优先从磁盘中读取缓存的原图，若没有原图再从网络中读取图片
4. 删除无效的url
