using Google.Protobuf.WellKnownTypes;
using Grpc.Core;
using Microsoft.AspNetCore.DataProtection.KeyManagement;
using UtilsLibrary;

namespace TransactionManager.Services
{
    public class TransactionServerInnerService : TransactionManagerInnerService.TransactionManagerInnerServiceBase
    {
        private string id;
        private int idNum;
        Dictionary<int, List<string>> dic;
        private Dictionary<string, int> dadInts = new Dictionary<string, int>();

        TransactionServerService service;


        public TransactionServerInnerService(int idNum, string id, Dictionary<int, List<string>> dic, TransactionServerService service) 
        {
            this.id = id;
            this.idNum = idNum;
            this.dic = dic;
            this.service = service;
        }

        public override Task<UpdateReply> Update(UpdateRequest request, ServerCallContext context)
        {
            if (!FileUtils.CheckIfCanReply(idNum, id, request.UpdaterId, dic[request.TimeSlot]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{id} is Unavailable!"));
            }

            return Task.FromResult(HandleUpdate(request));
        }

        public UpdateReply HandleUpdate(UpdateRequest request)
        {
            Console.WriteLine($"Received an Update from {request.UpdaterId}!");

            if (FileUtils.CheckIfCanReply(this.idNum, this.id, request.UpdaterId, dic[request.TimeSlot]))
            {
                var reply = new UpdateReply { UpdatedId = id, Accepted = true };
                
                return reply;
            }
         
            return null;
        }

        public override Task<CommitReply> Commit(CommitRequest request, ServerCallContext context)
        {
            return Task.FromResult(HandleCommit(request));
        }

        public CommitReply HandleCommit(CommitRequest request)
        {
            Console.WriteLine($"Received an Commit command from {request.CommiterId}!");

            lock (dadInts)
            {
                foreach (DadInt obj in request.Objects)
                {
                    dadInts[obj.Key] = obj.Value;
                }
            }

            var reply = new CommitReply { CommitedId = id, Commited = true };

            service.ReleaseLease(request.Keys.ToList());

            Console.WriteLine($"#{id} Releasing my own lease!");

            return reply;
        }

        public override Task<LeaseReleaseReply> LeaseRelease(LeaseReleaseRequest request, ServerCallContext context)
        {
            if (!FileUtils.CheckIfCanReply(idNum, id, request.OwnerId, dic[request.Slot]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{id} is Unavailable!"));
            }

            return Task.FromResult(HandleLeaseRelease(request));
        }

        public LeaseReleaseReply HandleLeaseRelease(LeaseReleaseRequest request)
        {
            service.ReleaseLease(request.Keys.ToList());

            var reply = new LeaseReleaseReply { ReceiverId = id, Accepted = true };

            return reply;
        }

        public override Task<AskReply> AskRelease(AskRequest request, ServerCallContext context)
        {
            return Task.FromResult(HandleAskRelease(request));
        }

        private AskReply HandleAskRelease(AskRequest request)
        {
            return new AskReply { Ack = true };
        }

    }
}
