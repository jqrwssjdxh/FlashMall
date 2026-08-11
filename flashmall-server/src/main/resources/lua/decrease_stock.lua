local stock = redis.call('get', KEYS[1])


if stock == false then
    return -1
end


if tonumber(stock) < tonumber(ARGV[1]) then
    return 0
end


redis.call(
    'decrby',
    KEYS[1],
    ARGV[1]
)


return 1