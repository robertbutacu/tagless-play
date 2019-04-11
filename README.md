# tagless-play
A small library which enables generic programming in Play.

The main problem with using a technique like Tagless Final and Play is the fact that the framework is bound to using Future everywhere where some kind of sync/async operation is performed. 

Because of this, in order to fully integrate them, there are 2 steps to be taken:
 1. have some natural transformations in scope: F[] ~> Future[] and most likely Future[] ~> F[] if the ReactiveMongoRepository is being used.
 2. wrap the functions which are generic - returning some F[A] - with the natural transformation so `toFuture: F ~> Future` is done in order to accomodate to Play's idea of Future[Result] in the controllers 
 
 It would look some along the lines of this:
 
   `override def delete(): Action[AnyContent] = Action.async {
      
      toFuture {
      
        withRecover {
       
          withValidJson[Tab] {
          
             tab => tabService.delete(tab).map(_ => Ok)
      
      }
   
   }`
   
   And the other way around for the repository aswell:
   
     override def remove(tab: Tab): F[Unit] = collection.flatMap(c => fromFuture{ c.findAndRemove(tab).map(_ => ()) } )
     
   Note that the collection is working in `F` as well:
    
    `def collection: F[JSONCollection] = fromFuture {
      reactiveMongoApi.database.map(_.collection("tabinder"))
    }`

It's easy to see how this becomes tedious: for every action/db call, there is a need to wrap it with the natural transformation to make the types work. Furthermore, should this technique be used intensively on a platform, there will be the same small bits of code which will be moved all throughout the projects.

And this is where this library comes in.

The main goal is to override the default behaviour using the said natural transformations and make it generic. 

For example, the controller will look like this:

  `override def delete(): Action[AnyContent] = GenericAction.async(parse.anyContent) {
     withRecover {
       withValidJson[Tab] {
         tab => tabService.delete(tab).map(_ => Ok)
       }
     }
   }`
   
The natural transformation still needs to be implicitly in scope, but no longer it is needed to wrap/unwrap from/to F[]/Future[].
   
