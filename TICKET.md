# MQ-7412 Fleet Board: support reports and one product ask

Digest from last week's support queue plus one thing product wants. Nobody has triaged this yet, so
treat the wording as what it is: what people told us, not a diagnosis.

## From support

**1. "Wrong labels on my bookings"**
Several drivers say that when they scroll through the booking list, bookings show a coloured dot or a
"Needs cleaning" label that does not belong to them. One driver sent a screenshot where a cancelled
booking was marked green. Scrolling back up and down again seems to change which rows are wrong.

**2. "I cannot reach the Start trip button"**
A driver reported that on their phone the Start trip button sits underneath the bar at the bottom of the
screen and is hard to hit. Support could not reproduce it on the test phone in the office.

**3. "The app hangs on a loading spinner"**
A driver tried to start a trip, got a spinner, and the app never came back. They could not tap anything
and had to force-quit. Support tried it on other bookings and it worked, so this was closed as
"not reproducible".

**4. "The app closes when I open a booking"**
A driver said the app closes immediately when they tap certain bookings in the list. Others work fine.

## From product

**5. Show who provides the vehicle, and help drivers focus**

> Drivers with a lot of bookings tell us the list is hard to scan. Two things would help: seeing which
> provider a vehicle belongs to without opening the booking, and being able to focus on the bookings
> that are relevant right now instead of everything they have ever booked.

That is the whole brief we got. If you need a decision that is not in there, make it and say so.

## Notes

- No design mocks exist for point 5. Match what is already in the app.
- The SDK in `sdk/` is a stand-in for a dependency owned by another team. Reading it is expected;
  changing it is a decision you should justify.
- There is more here than fits in three hours. Prioritise.
